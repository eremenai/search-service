import logging
from typing import List, Optional

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Embedding Calculation Service")

# Initialized at startup; kept module-scoped to avoid per-request loading.
model: Optional[SentenceTransformer] = None

PREVIEW_MAX_CHARS = 200


class EmbedRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Input text to embed")


class EmbedResponse(BaseModel):
    embedding: List[float]


@app.on_event("startup")
def load_model() -> None:
    """Load the embedding model once when the service starts."""
    global model
    logger.info("Loading embedding model 'BAAI/bge-small-en'...")
    model = SentenceTransformer("BAAI/bge-small-en-v1.5")
    logger.info("Model loaded.")


# Permissive CORS to simplify local/testing use; adjust for production.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/embed", response_model=EmbedResponse)
async def embed(request: EmbedRequest) -> EmbedResponse:
    """Generate an embedding for the provided text."""
    condensed_text = " ".join(request.text.split())
    preview = (
        condensed_text[: PREVIEW_MAX_CHARS - 3] + "..."
        if len(condensed_text) > PREVIEW_MAX_CHARS
        else condensed_text
    )
    logger.info("Received /embed request (length=%d, preview='%s')", len(request.text), preview)

    if not request.text.strip():
        raise HTTPException(status_code=400, detail="Field 'text' must be a non-empty string.")

    if model is None:
        logger.error("Model not loaded when handling request.")
        raise HTTPException(status_code=503, detail="Model not loaded yet.")

    try:
        embedding = model.encode(request.text, normalize_embeddings=True)
        return EmbedResponse(embedding=embedding.tolist())
    except HTTPException:
        # Re-raise FastAPI-generated HTTP errors untouched.
        raise
    except Exception as exc:
        logger.exception("Failed to generate embedding.")
        raise HTTPException(
            status_code=500, detail="Internal server error while generating embedding."
        ) from exc

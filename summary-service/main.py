import logging

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from transformers import pipeline

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Summarization Service")

summarizer = None


class SummarizeRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Input text to summarize")
    max_tokens: int = 128
    min_tokens: int = 32


class SummarizeResponse(BaseModel):
    summary: str


@app.on_event("startup")
def load_model():
    """
    Load a small summarization model once at startup.

    Model: sshleifer/distilbart-cnn-12-6
    - distilled version of BART CNN summarizer
    - smaller and faster than facebook/bart-large-cnn
    """
    global summarizer
    model = "sshleifer/distilbart-cnn-12-6"
    logger.info("Loading model %s" % model)
    summarizer = pipeline(
        "summarization",
        model=model,
        device_map="auto",  # use GPU if available, otherwise CPU
    )
    logger.info("Model loaded")

# Permissive CORS to simplify local/testing use; adjust for production.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/summarize", response_model=SummarizeResponse)
def summarize(req: SummarizeRequest):
    if summarizer is None:
        raise HTTPException(status_code=500, detail="Model not loaded")

    text = req.text.strip()
    if not text:
        raise HTTPException(status_code=400, detail="Empty text")

    # Simple guard against insane payloads
    if len(text) > 20000:
        raise HTTPException(status_code=400, detail="Text too long")

    result = summarizer(
        text,
        max_length=req.max_tokens,
        min_length=req.min_tokens,
        do_sample=False,
    )

    summary_text = result[0]["summary_text"]
    return SummarizeResponse(summary=summary_text)
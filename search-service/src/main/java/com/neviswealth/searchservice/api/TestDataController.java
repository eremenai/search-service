package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.service.ClientService;
import com.neviswealth.searchservice.service.DocumentService;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/populate-data")
public class TestDataController {

    private final ClientService clientService;
    private final DocumentService documentService;
    private final JdbcTemplate jdbcTemplate;

    public TestDataController(ClientService clientService, DocumentService documentService, JdbcTemplate jdbcTemplate) {
        this.clientService = clientService;
        this.documentService = documentService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/wipe-everything")
    public void wipeOut() {
        jdbcTemplate.execute("truncate table clients cascade");
    }

    @GetMapping("/simple")
    public void populate() {
        // Create clients
        ClientDto alexEremenkov = clientService.createClient(
                new CreateClientRequest("Alex", "Eremenkov", "eremen@gmail.com", "PT"));

        ClientDto johnSmyth = clientService.createClient(
                new CreateClientRequest("John", "Smyth", "john.smyth@neviswealth.com", "GB"));

        ClientDto joanaSilva = clientService.createClient(
                new CreateClientRequest("Joana", "Silva", "joana.silva@lisboa-mail.pt", "PT"));

        ClientDto miguelGomes = clientService.createClient(
                new CreateClientRequest("Miguel", "Gomes", "miguel.gomes@atlanticbank.com", "PT"));

        ClientDto sofiaDubois = clientService.createClient(
                new CreateClientRequest("Sofia", "Dubois", "sofia.dubois@paris-invest.fr", "FR"));

        ClientDto patrickOconnor = clientService.createClient(
                new CreateClientRequest("Patrick", "OConnor", "patrick.oconnor@greenfund.ie", "IE"));

        ClientDto andrzejKowalski = clientService.createClient(
                new CreateClientRequest("Andrzej", "Kowalski", "andrzej.kowalski@baltic-capital.pl", "PL"));

        ClientDto sarahMiller = clientService.createClient(
                new CreateClientRequest("Sarah", "Miller", "sarah.miller@citywealth.co.uk", "GB"));

        ClientDto inesFernandes = clientService.createClient(
                new CreateClientRequest("Ines", "Fernandes", "ines.fernandes@tejoadvisors.pt", "PT"));

        ClientDto aleksanderEremenko = clientService.createClient(
                new CreateClientRequest("Aleksander", "Eremenko", "aleksander.eremenko@neviswealth.com", "PT"));


// Documents for Alex (2 docs)
        documentService.createDocument(
                alexEremenkov.id(),
                new CreateDocumentRequest(
                        "Utility bill October 2025",
                        "Electricity utility bill for October 2025 used as proof of address for Alex Eremenkov."
                )
        );

        documentService.createDocument(
                alexEremenkov.id(),
                new CreateDocumentRequest(
                        "Passport scan",
                        "Scan of the passport for identity verification. Contains full name, date of birth and nationality."
                )
        );


// Documents for John Smyth (3 docs)
        documentService.createDocument(
                johnSmyth.id(),
                new CreateDocumentRequest(
                        "Utility bill September 2025",
                        "Gas utility bill for September 2025. This document serves as proof of address for John Smyth."
                )
        );

        documentService.createDocument(
                johnSmyth.id(),
                new CreateDocumentRequest(
                        "Bank statement Q3 2025",
                        "Quarterly bank statement for Q3 2025 showing income, regular transfers and account balance."
                )
        );

        documentService.createDocument(
                johnSmyth.id(),
                new CreateDocumentRequest(
                        "Employment contract",
                        "Signed employment contract with Nevis Wealth describing position, salary and start date."
                )
        );


// Documents for Joana Silva (1 doc)
        documentService.createDocument(
                joanaSilva.id(),
                new CreateDocumentRequest(
                        "Water bill August 2025",
                        "Water utility bill for August 2025 issued to Joana Silva at her current residential address."
                )
        );


// Documents for Miguel Gomes (0 docs) – intentionally none


// Documents for Sofia Dubois (2 docs)
        documentService.createDocument(
                sofiaDubois.id(),
                new CreateDocumentRequest(
                        "Proof of address letter",
                        "Formal proof of address letter from the local council confirming residence of Sofia Dubois."
                )
        );

        documentService.createDocument(
                sofiaDubois.id(),
                new CreateDocumentRequest(
                        "Payslip October 2025",
                        "Monthly payslip for October 2025 showing net salary, taxes and employer details."
                )
        );


// Documents for Patrick O'Connor (1 doc)
        documentService.createDocument(
                patrickOconnor.id(),
                new CreateDocumentRequest(
                        "Electricity bill July 2025",
                        "Electricity utility bill for July 2025 which can be used as address proof for onboarding."
                )
        );


// Documents for Andrzej Kowalski (3 docs)
        documentService.createDocument(
                andrzejKowalski.id(),
                new CreateDocumentRequest(
                        "Mortgage statement 2025",
                        "Annual mortgage statement for 2025 summarising outstanding balance and monthly instalments."
                )
        );

        documentService.createDocument(
                andrzejKowalski.id(),
                new CreateDocumentRequest(
                        "Proof of address – utility bill",
                        "Combined internet and TV utility bill explicitly marked as proof of address for the account holder."
                )
        );

        documentService.createDocument(
                andrzejKowalski.id(),
                new CreateDocumentRequest(
                        "National ID card copy",
                        "Copy of national ID card including photo, ID number and expiry date."
                )
        );


// Documents for Sarah Miller (0 docs) – intentionally none


// Documents for Ines Fernandes (2 docs)
        documentService.createDocument(
                inesFernandes.id(),
                new CreateDocumentRequest(
                        "Rent contract 2024–2025",
                        "Residential rental agreement covering the period from September 2024 to August 2025."
                )
        );

        documentService.createDocument(
                inesFernandes.id(),
                new CreateDocumentRequest(
                        "Bank statement October 2025",
                        "Bank statement for October 2025 showing regular salary payments and account activity."
                )
        );


// Documents for Aleksander Eremenko (1 doc)
        documentService.createDocument(
                aleksanderEremenko.id(),
                new CreateDocumentRequest(
                        "Internet bill November 2025",
                        "Internet service utility bill for November 2025, accepted as proof of address by most institutions."
                )
        );

    }

    @GetMapping("/complex")
    public void populateComplex() {
        // New test clients (all different from previous ones)
        ClientDto emmaJones = clientService.createClient(
                new CreateClientRequest("Emma", "Jones", "emma.jones@shoreviewwealth.com", "GB"));

        ClientDto carlosPereira = clientService.createClient(
                new CreateClientRequest("Carlos", "Pereira", "carlos.pereira@tagus-advisors.pt", "PT"));

        ClientDto lukasNovak = clientService.createClient(
                new CreateClientRequest("Lukas", "Novak", "lukas.novak@danubecapital.eu", "CZ"));

        ClientDto fatimaRahman = clientService.createClient(
                new CreateClientRequest("Fatima", "Rahman", "fatima.rahman@crescentadvisors.co.uk", "GB"));

        ClientDto oliverBrown = clientService.createClient(
                new CreateClientRequest("Oliver", "Brown", "oliver.brown@harborstonebank.com", "IE"));

        ClientDto martaRossi = clientService.createClient(
                new CreateClientRequest("Marta", "Rossi", "marta.rossi@alpineawealth.it", "IT"));

        ClientDto jonasSchneider = clientService.createClient(
                new CreateClientRequest("Jonas", "Schneider", "jonas.schneider@rhein-invest.de", "DE"));

        ClientDto helenaCosta = clientService.createClient(
                new CreateClientRequest("Helena", "Costa", "helena.costa@atlanticbridge.pt", "PT"));

        // Additional documents to bring your dataset up to ~30 diverse KYC / non-KYC docs.

// Emma Jones – identity, income follow-up, misc identity-related
        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Video identification session log – Emma Jones",
                        """
                        This log records the remote video identification session conducted with Emma Jones. The analyst verified that
                        the person on the call matched the photograph on the passport and that the name, date of birth and document
                        number were consistent with the copy previously uploaded to the system. During the call, Emma was asked to tilt
                        the passport to show holographic security features and to read out specific details from the biographical page.
        
                        The session notes confirm that the internet connection remained stable, no third parties appeared on screen and
                        there were no suspicious delays when Emma was asked to perform simple actions such as turning the document or
                        covering parts of it with a finger. The analyst concluded that the video identification successfully supports
                        the passport as a genuine proof of identity for Emma Jones.
                        """
                )
        );

        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Email follow-up – missing payslip for September",
                        """
                        This document summarises the email exchange between the onboarding team and Emma Jones regarding a missing
                        payslip. Emma had initially provided the August and October 2025 salary slips, but the September payslip was
                        not included. The first email politely requested that she upload or email the missing document so that her
                        income history would be complete for the three-month assessment window.
        
                        Emma replied that she had temporarily misplaced the PDF, but would download it again from the employer portal
                        and forward it to the team. The thread concludes with a confirmation from the analyst that the September
                        payslip was received and successfully added to the “proof of income” documentation set. The emails are not
                        used as proof of income themselves; they simply document the request and completion of the requirement.
                        """
                )
        );

        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Name change note – passport vs employer records",
                        """
                        This note explains a minor discrepancy in Emma Jones's documentation where the employer records still show
                        her previous surname. Emma recently reverted from a double-barrelled surname to her original maiden name,
                        which is now reflected on her passport and bank account. However, the HR system at Shoreview Wealth still
                        displays the older version of her name in the internal directory and on payslips.
        
                        The analyst records that Emma provided a copy of the official name change certificate and that the passport
                        and bank details are aligned with the new name. The employer has confirmed that payroll will be updated at
                        the next cycle. This note clarifies that the discrepancy is purely administrative and does not represent a
                        KYC risk, and it is stored as an identity reconciliation comment rather than a separate proof document.
                        """
                )
        );


// Carlos Pereira – employment checks, income stability, PEP screening
        documentService.createDocument(
                carlosPereira.id(),
                new CreateDocumentRequest(
                        "HR verification call summary – Tagus Advisors",
                        """
                        This summary documents a verification call placed by the onboarding team to the HR department of Tagus Advisors
                        regarding the employment of Carlos Pereira. The HR contact confirmed Carlos's job title, start date, permanent
                        contract status and that he is employed on a full-time basis. No discrepancies were identified between the HR
                        confirmation and the employment contract previously submitted by Carlos.
        
                        The HR representative also confirmed that there were no known plans to terminate or materially alter Carlos's
                        contract at the time of the call. The analyst concludes that the external verification supports the authenticity
                        of the employment documents and that the employment data can be relied upon for affordability and suitability
                        analysis.
                        """
                )
        );

        documentService.createDocument(
                carlosPereira.id(),
                new CreateDocumentRequest(
                        "Internal credit committee note – income stability assessment",
                        """
                        This note reflects the internal credit committee’s discussion about the stability of Carlos Pereira’s income.
                        The committee reviewed his three-month payslip history, the employer letter and the HR verification call
                        summary. They noted that Carlos’s base salary is substantial and regular, and that his bonus payments have
                        been consistent over several years according to previous account statements.
        
                        The committee concluded that Carlos’s income is stable enough to support the level of investment and credit
                        products requested, provided that his existing commitments remain unchanged. The note records that this
                        conclusion is based purely on income stability and does not address other KYC dimensions such as address,
                        tax residency or source of funds, which are documented separately.
                        """
                )
        );

        documentService.createDocument(
                carlosPereira.id(),
                new CreateDocumentRequest(
                        "Client declaration – no political exposure",
                        """
                        This document records a signed declaration from Carlos Pereira stating that neither he nor any close family
                        members or associates hold prominent public functions that would make him a politically exposed person (PEP).
                        The declaration was completed as part of the standard onboarding process and is stored together with other
                        AML and sanctions-related documentation.
        
                        The compliance officer reviewed publicly available information and internal PEP screening results, finding
                        no evidence that contradicts Carlos’s declaration. As a result, Carlos is classified as a non-PEP client.
                        The document is tagged under “PEP / AML declarations” and is not used for proof of address, income or tax
                        residency purposes.
                        """
                )
        );


// Lukas Novak – enhanced due diligence, investment knowledge, travel notes
        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "Enhanced due diligence note – apartment sale proceeds",
                        """
                        This enhanced due diligence note explains the assessment performed on the large inflow of funds from the sale
                        of an apartment owned by Lukas Novak. The first section references the notarised sale contract and the land
                        registry extract showing that Lukas was the legal owner of the property prior to the sale. The bank statement
                        is reviewed to confirm that the incoming transfer matches the amount stated in the sale contract.
        
                        The second section documents the analyst’s conclusion that the transaction is consistent with Lukas’s declared
                        source of wealth. No unusual pricing or hidden counterparties were identified, and the funds were transferred
                        directly from the buyer’s bank to Lukas’s account. The note records that this inflow is treated as legitimate
                        property-sale proceeds and is fully acceptable as a source of funds for subsequent investments.
                        """
                )
        );

        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "Investment experience and knowledge questionnaire – summary",
                        """
                        This summary captures the key points from Lukas Novak’s investment experience and knowledge questionnaire.
                        Lukas reports several years of experience investing in mutual funds and listed equities, and occasional use
                        of simple exchange-traded funds. He states that he understands basic concepts such as diversification, market
                        volatility and long-term investing.
        
                        However, Lukas indicates that he has little to no experience with complex products such as options, futures,
                        leveraged ETFs or structured notes. The analyst categorises his knowledge level as “intermediate” and notes
                        that any recommendations should focus on plain-vanilla funds and securities. The document is tagged under
                        “knowledge and experience” and is used in conjunction with his risk profile to determine product suitability.
                        """
                )
        );

        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "Travel history note – unrelated to tax residency",
                        """
                        This note briefly records a conversation in which Lukas Novak mentioned frequent short trips to various
                        countries for conferences and leisure. He described attending technology events in different European cities
                        and occasionally extending his stays for tourism. The note clarifies that these trips are short-term visits
                        and do not imply additional tax residency obligations.
        
                        The compliance officer explicitly records that, based on the information provided, Lukas’s travel history is
                        not considered relevant for CRS or FATCA classification. The note is stored only as contextual information and
                        must not be interpreted as an indication of multiple tax residencies or permanent establishment in other
                        jurisdictions.
                        """
                )
        );


// Fatima Rahman – suitability, reminders, non-KYC complaint
        documentService.createDocument(
                fatimaRahman.id(),
                new CreateDocumentRequest(
                        "Suitability assessment summary – multi-asset mandate",
                        """
                        This document summarises the suitability assessment performed for Fatima Rahman in relation to a discretionary
                        multi-asset mandate. The analysis takes into account her risk profile, investment horizon, income stability and
                        existing financial commitments. The first section explains why a diversified blend of equity and bond funds is
                        considered appropriate given her “balanced to growth” risk category.
        
                        The second section evaluates whether the proposed mandate is consistent with Fatima’s stated objectives and
                        capacity for loss. Stress tests and historical drawdown scenarios are described, and it is noted that the
                        potential losses in adverse markets remain within the bounds Fatima indicated as tolerable. The document is
                        tagged as “suitability assessment” and is distinct from basic KYC documents such as identity or address proofs.
                        """
                )
        );

        documentService.createDocument(
                fatimaRahman.id(),
                new CreateDocumentRequest(
                        "Email reminder thread – outstanding self-certification",
                        """
                        This thread summarises a series of reminder emails sent to Fatima Rahman regarding an outstanding tax
                        self-certification form. The initial email politely requested that she complete the CRS/FATCA form to confirm
                        her tax residency status. Follow-up messages were sent at weekly intervals, each reiterating that the form was
                        required before certain account features could be activated.
        
                        Eventually, Fatima replied with an apology for the delay and attached the completed and signed self-certification.
                        The analyst recorded the date of receipt and marked the tax residency requirement as fulfilled. The emails
                        themselves are not treated as tax residency evidence; they simply document the chase process and resolution.
                        """
                )
        );

        documentService.createDocument(
                fatimaRahman.id(),
                new CreateDocumentRequest(
                        "Service complaint log – mobile app login issue",
                        """
                        This log relates to a service complaint raised by Fatima Rahman about difficulties logging into the mobile
                        application. The first entry notes that she experienced repeated login failures and error messages when trying
                        to access her portfolio on a Sunday evening. Support staff captured screenshots and device information to help
                        reproduce the issue.
        
                        Subsequent entries record the investigation and resolution steps, including an update from IT that a temporary
                        authentication outage had affected a subset of users. Once the issue was resolved, Fatima confirmed that she
                        could log in again without problems. The log is explicitly marked as non-KYC and non-advisory; it is purely
                        operational and has no bearing on identity, address, income, risk or tax residency assessments.
                        """
                )
        );


// Oliver Brown – sanctions, CRS data review, investment meeting
        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "Sanctions and watchlist screening result – Oliver Brown",
                        """
                        This document records the outcome of the sanctions and adverse-media screening checks performed for Oliver
                        Brown. The screening tool searched multiple sanctions lists, law-enforcement databases and news sources for
                        matches to Oliver’s name, date of birth and residency details. Several potential name-only hits were reviewed
                        and dismissed as false positives because key identifiers did not match.
        
                        The final section confirms that no relevant sanctions, law-enforcement or negative media entries were found
                        for Oliver at the time of onboarding. The result is classified as “sanctions screening – clear” and is stored
                        under AML documentation. It is explicitly noted that this document is unrelated to proof of address, income
                        or tax residency; it solely reflects the outcome of sanctions and watchlist checks.
                        """
                )
        );

        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "CRS data quality review note – Oliver Brown",
                        """
                        This note describes a periodic review of CRS data quality for the client Oliver Brown. The reviewer compared
                        the tax residency and taxpayer identification number stored in the system with the most recent self-certification
                        and identification documents on file. No inconsistencies were found between Oliver’s declared tax residency,
                        his address and the country of issue of his passport.
        
                        The reviewer also checked that Oliver’s account is correctly flagged in the reporting engine and that no
                        obsolete secondary residencies remain in the data. The note concludes that the CRS data set is accurate and
                        up-to-date. It is stored as “CRS data quality review” and is separate from broader onboarding documents such
                        as proof of address or risk profile.
                        """
                )
        );

        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "Meeting minutes – investment performance review",
                        """
                        These minutes summarise a scheduled meeting with Oliver Brown focusing on the performance of his existing
                        investment portfolio. The first part recaps recent market developments and how they affected his equity and
                        bond holdings. Oliver expressed mild concern about short-term volatility but confirmed that his long-term
                        objectives and risk tolerance remain unchanged.
        
                        The second part lists agreed follow-up actions, including a minor rebalancing of certain fund positions and
                        a review of fees for an underperforming product. No changes were made to Oliver’s risk profile, tax residency
                        or KYC status during this meeting. The document is categorised as “advice / performance review” and is not
                        used as identity or address documentation.
                        """
                )
        );


// Marta Rossi – source of wealth, communication preferences, onboarding timeline
        documentService.createDocument(
                martaRossi.id(),
                new CreateDocumentRequest(
                        "Source of wealth note – family business share sale",
                        """
                        This note summarises the explanation provided by Marta Rossi regarding a significant part of her wealth. Marta
                        reports that she held a minority share in a family-owned logistics company in northern Italy. When the company
                        was sold to a larger international group, she received a lump-sum payment as part of the transaction.
        
                        The onboarding team reviewed the share purchase agreement, payment confirmation and historic dividend records
                        to verify that this explanation is plausible and supported by documentation. The note concludes that the sale
                        of the family business represents a legitimate and well-documented source of wealth for Marta, distinct from
                        her regular employment income.
                        """
                )
        );

        documentService.createDocument(
                martaRossi.id(),
                new CreateDocumentRequest(
                        "Communication preferences and language note",
                        """
                        This document records Marta Rossi’s communication preferences as captured during onboarding. She requested that
                        formal documentation such as contracts and legal notices be provided in Italian wherever possible, while being
                        comfortable with informal emails in English. She also indicated a preference for email over phone calls for
                        non-urgent matters.
        
                        The note clarifies that these preferences do not affect the validity of KYC documents; they only guide how
                        future information and disclosures should be presented. It is tagged as “communication preferences” and is
                        unrelated to proof of identity, address, income, risk or tax residency.
                        """
                )
        );

        documentService.createDocument(
                martaRossi.id(),
                new CreateDocumentRequest(
                        "Onboarding timeline explanation – delayed document delivery",
                        """
                        This note explains the slightly extended onboarding timeline for Marta Rossi. Due to travel and work
                        commitments, Marta needed additional time to obtain certain documents, including translated versions of her
                        Italian tax papers. The onboarding team agreed an extended deadline and documented interim checks to ensure
                        that no accounts would be activated prematurely.
        
                        Once all required documents were received and validated, the onboarding status was updated to “complete”.
                        The note makes clear that the delay was logistical rather than risk-driven, and that no additional red flags
                        were identified as a result of the extended timeline.
                        """
                )
        );


// Jonas Schneider – AML alerts, crypto explanation, random hiking chat
        documentService.createDocument(
                jonasSchneider.id(),
                new CreateDocumentRequest(
                        "AML monitoring alert review – large incoming transfer",
                        """
                        This document records the review of an automated AML monitoring alert triggered by a large incoming transfer
                        to Jonas Schneider’s account. The transfer originated from another EU bank and was flagged because the amount
                        exceeded the usual pattern of monthly inflows for Jonas. The analyst reviewed the payment reference and found
                        that it matched a previously documented sale of an investment position.
        
                        After examining supporting documents and confirming that the source of funds had already been assessed and
                        recorded, the analyst closed the alert as “explained and acceptable”. The note emphasises that the transaction
                        is consistent with Jonas’s known financial profile and that no escalation is required. It is stored as part of
                        ongoing AML monitoring records.
                        """
                )
        );

        documentService.createDocument(
                jonasSchneider.id(),
                new CreateDocumentRequest(
                        "Client follow-up – explanation of crypto cash-out",
                        """
                        This note summarises a follow-up call during which Jonas Schneider explained a one-off cash-out from a crypto
                        exchange that appeared in his transaction history. Jonas stated that the crypto position had been built over
                        several years using small contributions from his salary and that he decided to liquidate the position when
                        the price reached a personal target.
        
                        The analyst requested and received a statement from the exchange showing the transaction history and the final
                        withdrawal to Jonas’s bank account. Based on this additional information, the cash-out is recorded as a
                        legitimate source of funds with a clear audit trail. The note is tagged as “crypto – source of funds”.
                        """
                )
        );

        documentService.createDocument(
                jonasSchneider.id(),
                new CreateDocumentRequest(
                        "Random chat notes – hiking gear and weekend plans",
                        """
                        These notes capture a short, informal conversation with Jonas Schneider in which no financial or KYC topics
                        were discussed. Jonas talked about testing a new hiking backpack and comparing different types of trail
                        running shoes. He also mentioned considering a weekend trip to the mountains if the weather allowed.
        
                        The notes are explicitly marked as non-KYC and non-advisory. They are stored only as background context about
                        personal interests and must not be used as evidence for identity, address, income, risk or any regulatory
                        requirement.
                        """
                )
        );


// Helena Costa – tax residency clarification, form help, office visit log
        documentService.createDocument(
                helenaCosta.id(),
                new CreateDocumentRequest(
                        "Tax residency clarification – move between Portugal and Spain",
                        """
                        This note documents a clarification call with Helena Costa regarding her tax residency status. Helena explained
                        that she had temporarily lived and worked in Spain for part of the previous year but has since returned to
                        Portugal and is now registered as a Portuguese tax resident. She provided her Portuguese taxpayer number and
                        confirmed that she no longer files tax returns in Spain.
        
                        The compliance officer checked that the most recent self-certification reflects Portugal as the only current
                        tax residency and that previous Spanish residency has been correctly marked as historical. The note concludes
                        that Helena should be treated as tax resident in Portugal only for CRS and FATCA purposes as of the current
                        year.
                        """
                )
        );

        documentService.createDocument(
                helenaCosta.id(),
                new CreateDocumentRequest(
                        "Form completion helper note – guidance for KYC questions",
                        """
                        This helper note records guidance given to Helena Costa when she asked for clarification on certain KYC form
                        questions. The analyst explained the difference between “source of funds” and “source of wealth”, and gave
                        examples of how to describe employment income, business proceeds and inheritances without disclosing excessive
                        personal detail.
        
                        The note emphasises that the guidance was purely explanatory and that Helena’s answers must reflect her own
                        situation. It is stored as a support record and is not treated as a signed statement or proof document in its
                        own right.
                        """
                )
        );

        documentService.createDocument(
                helenaCosta.id(),
                new CreateDocumentRequest(
                        "Office visit log – coffee and non-financial chat",
                        """
                        This log records a short office visit by Helena Costa during which she dropped off some original documents for
                        scanning. While the scanner was in use, Helena had a casual chat with the receptionist about local cafés,
                        weekend markets and a new exhibition at the city museum. No financial topics, product discussions or KYC
                        details were covered.
        
                        The visit is recorded solely for physical access tracking and document handling purposes. The conversation
                        notes are explicitly marked as non-KYC and non-advisory, and they play no role in onboarding, suitability or
                        AML assessments.
                        """
                )
        );


    }
}
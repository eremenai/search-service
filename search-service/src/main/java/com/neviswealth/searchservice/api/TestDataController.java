package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.api.dto.CreateDocumentRequest;
import com.neviswealth.searchservice.service.ClientService;
import com.neviswealth.searchservice.service.DocumentService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/populate-data")
public class TestDataController {

    private final ClientService clientService;
    private final DocumentService documentService;

    public TestDataController(ClientService clientService, DocumentService documentService) {
        this.clientService = clientService;
        this.documentService = documentService;
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


// Emma Jones – 3 documents
        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Electricity bill November 2025 – proof of address",
                        """
                        Monthly electricity bill for the billing period 01–30 November 2025 for Emma Jones.
                        The invoice lists the supply address in Manchester, the account number, meter readings and total amount due.
                        It clearly shows Emma Jones as the account holder and repeats her residential address on the first page and on the payment slip.
                        This electricity bill is typically accepted by banks and wealth managers as a primary proof of address,
                        provided it is recent (usually not older than three months) and shows the same address as used during onboarding.
                        """
                )
        );

        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Current account statement October 2025",
                        """
                        Current account statement for Emma Jones covering the period from 1 October 2025 to 31 October 2025.
                        The statement lists all incoming salary credits, card transactions, standing orders and transfers between accounts.
                        In the header, the document displays Emma's full name and correspondence address, along with the IBAN and BIC of the account.
                        Some institutions accept a recent bank statement as an alternative address proof when a utility bill is not available,
                        but the primary classification of this document is a financial statement and evidence of banking activity.
                        """
                )
        );

        documentService.createDocument(
                emmaJones.id(),
                new CreateDocumentRequest(
                        "Employer letter – role and income confirmation",
                        """
                        Formal employer letter issued by Shoreview Wealth confirming the role and income of Emma Jones.
                        The letter states her job title, permanent employment status, gross annual salary and the date she joined the company.
                        It is signed on company letterhead that includes the employer's address and contact information.
                        This letter is considered proof of income and employment rather than proof of address,
                        although the presence of the company address and the employee's details can be used as supporting information in some jurisdictions.
                        """
                )
        );


// Carlos Pereira – 2 documents
        documentService.createDocument(
                carlosPereira.id(),
                new CreateDocumentRequest(
                        "Water utility bill August 2025",
                        """
                        Water utility bill issued to Carlos Pereira for his apartment in Lisbon for the period 01–31 August 2025.
                        The bill shows the supply address, contract number, meter readings and a detailed breakdown of water and sewage charges.
                        It identifies Carlos as the contract holder and states clearly that it is a residential service account.
                        This document is commonly accepted as a utility bill that can serve as proof of address for regulatory checks and onboarding.
                        """
                )
        );

        documentService.createDocument(
                carlosPereira.id(),
                new CreateDocumentRequest(
                        "Portuguese tenancy agreement 2024–2026",
                        """
                        Fixed-term tenancy agreement for Carlos Pereira covering the rental period from 1 September 2024 to 31 August 2026.
                        The contract specifies the rented property address in Lisbon, monthly rent, deposit amount, and responsibilities for utilities.
                        It is signed by both the landlord and Carlos, and includes identification details for both parties.
                        While the primary purpose is to define the rental relationship, many compliance teams accept a signed tenancy contract
                        as supporting proof of address, especially when utility bills are not yet available or are issued in the landlord's name.
                        """
                )
        );


// Lukas Novak – 3 documents
        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "Gas bill September 2025 – residential supply",
                        """
                        Gas bill for Lukas Novak for the billing period 01–30 September 2025.
                        The invoice lists the supply address in Prague, customer number, meter reading history and applicable tariffs.
                        It clearly labels the account as a residential customer account and includes Lukas as the named contract holder.
                        Financial institutions frequently treat such a gas bill as valid address verification documentation when onboarding a new client.
                        """
                )
        );

        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "Investment portfolio statement Q3 2025",
                        """
                        Quarterly investment portfolio statement for Lukas Novak issued by Danube Capital.
                        The report summarises holdings in funds, bonds and equities, including valuations, performance and risk metrics.
                        It contains Lukas's name and a correspondence address, but the primary focus of the document is on investment positions
                        and historical performance rather than address verification.
                        It is generally not considered a primary proof of address but may be used as a secondary supporting document.
                        """
                )
        );

        documentService.createDocument(
                lukasNovak.id(),
                new CreateDocumentRequest(
                        "National ID card copy – Czech Republic",
                        """
                        Copy of the Czech national ID card belonging to Lukas Novak.
                        The card shows his full name, date of birth, nationality and permanent residential address, along with a unique ID number.
                        This document is primarily used to verify identity, but in some cases the address printed on the card is also accepted
                        as proof of address by banks and brokers, depending on local regulatory requirements.
                        """
                )
        );


// Fatima Rahman – 2 documents
        documentService.createDocument(
                fatimaRahman.id(),
                new CreateDocumentRequest(
                        "Council tax bill 2025–2026",
                        """
                        Annual council tax bill for the property occupied by Fatima Rahman for the tax year 2025–2026.
                        The bill shows the property address in Birmingham, council reference number, and the total annual amount payable in instalments.
                        It is addressed directly to Fatima and is considered a strong document for verifying residential address.
                        Many institutions treat a council tax bill as equivalent to a utility bill in terms of address verification strength.
                        """
                )
        );

        documentService.createDocument(
                fatimaRahman.id(),
                new CreateDocumentRequest(
                        "Salary slip October 2025",
                        """
                        Monthly salary slip for October 2025 for Fatima Rahman, employed as a senior analyst at Crescent Advisors.
                        The payslip details gross salary, deductions (tax, national insurance, pension contributions) and net pay received.
                        It includes the employer's address and payroll contact information.
                        This document is used as proof of income and is generally not sufficient on its own as proof of address,
                        although some lenders ask for both a payslip and a recent utility bill as part of their checks.
                        """
                )
        );


// Oliver Brown – 3 documents
        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "Internet bill October 2025 – address verification candidate",
                        """
                        Monthly internet service bill for Oliver Brown for October 2025.
                        The invoice lists the installation address in Dublin, the customer reference, service description and subscription charges.
                        Although internet bills are not always treated as traditional utility bills, many financial institutions accept them
                        as an address verification document when other utility bills (electricity, gas, water) are not available.
                        """
                )
        );

        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "Proof of address guidance note",
                        """
                        Internal guidance note for Oliver Brown outlining which documents the firm accepts as proof of address.
                        The note lists electricity bills, gas bills, water bills, council tax bills and bank statements as acceptable documents.
                        It explicitly mentions that a recent utility bill or council tax bill not older than three months should be provided,
                        and that online printouts may need to show a full address and client name.
                        The note itself is not a proof of address, but it clearly describes the role a utility bill plays in address verification.
                        """
                )
        );

        documentService.createDocument(
                oliverBrown.id(),
                new CreateDocumentRequest(
                        "Harborstone Bank account opening confirmation",
                        """
                        Confirmation letter from Harborstone Bank confirming that Oliver Brown's current account has been successfully opened.
                        The letter references the account number, opening date and branch location.
                        It repeats the correspondence address provided by Oliver at onboarding, but the main purpose of the letter is to confirm account setup.
                        It is generally not treated as a primary proof of address unless explicitly specified by the bank's policies.
                        """
                )
        );


// Marta Rossi – 1 document
        documentService.createDocument(
                martaRossi.id(),
                new CreateDocumentRequest(
                        "Rental contract Milan 2025–2027",
                        """
                        Residential rental contract for Marta Rossi for an apartment in Milan, covering the period from March 2025 to February 2027.
                        The contract lists the property address, monthly rent, deposit, termination conditions and obligations for utilities.
                        Both landlord and tenant have signed, with identification details provided for each.
                        In practice, this contract can be used as supporting evidence of address, especially in combination with a recent utility bill.
                        """
                )
        );


// Jonas Schneider – 2 documents
        documentService.createDocument(
                jonasSchneider.id(),
                new CreateDocumentRequest(
                        "Combined energy bill September 2025",
                        """
                        Combined electricity and gas bill for Jonas Schneider for September 2025.
                        The bill lists the supply address in Cologne, customer number, consumption data for both electricity and gas, and total charges.
                        It indicates that the account is a private household account.
                        This combined energy bill is regarded as a strong proof of address document for regulatory and onboarding checks.
                        """
                )
        );

        documentService.createDocument(
                jonasSchneider.id(),
                new CreateDocumentRequest(
                        "Rhein Invest portfolio overview",
                        """
                        Portfolio overview for Jonas Schneider prepared by Rhein Invest.
                        The report outlines his holdings in several investment funds, including risk categories and historical performance.
                        It lists Jonas's name and a mailing address, but the report is not positioned as a document for address verification.
                        Instead, it is used to explain investment positions and to support suitability assessments.
                        """
                )
        );


// Helena Costa – 2 documents
        documentService.createDocument(
                helenaCosta.id(),
                new CreateDocumentRequest(
                        "Water and sewage bill October 2025",
                        """
                        Water and sewage bill for Helena Costa for the month of October 2025.
                        The invoice shows the service address in Porto, contract number, consumption volume, and tariff details.
                        It is clearly addressed to Helena and is considered a classic utility bill that can act as proof of address.
                        When combined with an identity document, it typically satisfies most onboarding address verification requirements.
                        """
                )
        );

        documentService.createDocument(
                helenaCosta.id(),
                new CreateDocumentRequest(
                        "Atlantic Bridge onboarding checklist",
                        """
                        Onboarding checklist for Helena Costa prepared by Atlantic Bridge.
                        The checklist explains that a recent utility bill, council tax bill or bank statement is required as proof of address,
                        and that payslips and employment letters are requested as proof of income.
                        It lists examples such as 'electricity bill', 'water bill' and 'gas bill' as acceptable address documents.
                        The checklist itself is not a proof document, but it clarifies how a utility bill is used as address proof in the onboarding process.
                        """
                )
        );

    }
}
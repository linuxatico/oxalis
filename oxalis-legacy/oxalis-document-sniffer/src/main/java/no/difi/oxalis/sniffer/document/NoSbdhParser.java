/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.sniffer.document;

import no.difi.oxalis.sniffer.PeppolStandardBusinessHeader;
import no.difi.oxalis.sniffer.document.parsers.PEPPOLDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

/**
 * Parses UBL based documents, which are not wrapped within an SBDH, extracting data and
 * creating a PeppolStandardBusinessHeader.
 *
 * @author steinar
 * @author thore
 */
public class NoSbdhParser {

    public static final Logger log = LoggerFactory.getLogger(NoSbdhParser.class);

    private final DocumentBuilderFactory documentBuilderFactory;

    public NoSbdhParser() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to configure DOM parser for secure processing.", e);
        }
    }

    /**
     * Parses and extracts the data needed to create a PeppolStandardBusinessHeader object. The inputstream supplied
     * should not be wrapped in an SBDH.
     *
     * @param inputStream UBL XML data without an SBDH.
     * @return an instance of PeppolStandardBusinessHeader populated with data from the UBL XML document.
     */
    public PeppolStandardBusinessHeader parse(InputStream inputStream) {

        try {

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new HardCodedNamespaceResolver());

            PeppolStandardBusinessHeader sbdh = PeppolStandardBusinessHeader
                    .createPeppolStandardBusinessHeaderWithNewDate();

            // use the plain UBL header parser to decode format and create correct document parser
            PlainUBLHeaderParser headerParser = new PlainUBLHeaderParser(document, xPath);

            // make sure we actually have a UBL type document
            if (headerParser.canParse()) {

                sbdh.setDocumentTypeIdentifier(headerParser.fetchDocumentTypeId().toVefa());
                sbdh.setProfileTypeIdentifier(headerParser.fetchProcessTypeId());

                // try to use a specialized document parser to fetch more document details
                PEPPOLDocumentParser documentParser = null;
                try {
                    documentParser = headerParser.createDocumentParser();
                } catch (Exception ex) {
                    /*
                        allow this to happen so that "unknown" PEPPOL documents still
                        can be used by explicitly setting sender and receiver thru API
                    */
                }
                /* However, if we found an eligible parser, we should be able to determine the sender and receiver */
                if (documentParser != null) {
                    try {
                        sbdh.setSenderId(documentParser.getSender());
                    } catch (Exception e) {
                        // Continue with recipient
                    }
                    try {
                        sbdh.setRecipientId(documentParser.getReceiver());
                    } catch (Exception e) {
                        // Just continue
                    }
                }
            }

            return sbdh;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse document " + e.getMessage(), e);
        }
    }
}

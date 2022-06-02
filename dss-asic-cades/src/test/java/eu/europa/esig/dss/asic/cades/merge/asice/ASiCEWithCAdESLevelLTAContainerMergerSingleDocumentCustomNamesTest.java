package eu.europa.esig.dss.asic.cades.merge.asice;

import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.cades.SimpleASiCWithCAdESFilenameFactory;
import eu.europa.esig.dss.asic.cades.merge.ASiCEWithCAdESContainerMerger;
import eu.europa.esig.dss.asic.cades.merge.AbstractWithCAdESTestMerge;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.common.merge.ASiCContainerMerger;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlManifestFile;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.ArchiveTimestampType;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ASiCEWithCAdESLevelLTAContainerMergerSingleDocumentCustomNamesTest extends AbstractWithCAdESTestMerge {

    private DSSDocument documentToSign;

    private ASiCWithCAdESService service;

    private ASiCWithCAdESSignatureParameters firstSignatureParameters;
    private ASiCWithCAdESSignatureParameters secondSignatureParameters;

    @BeforeEach
    public void init() {
        documentToSign = new InMemoryDocument("Hello World!".getBytes(), "test.txt", MimeType.TEXT);

        service = new ASiCWithCAdESService(getCompleteCertificateVerifier());
        service.setTspSource(getGoodTsa());

        firstSignatureParameters = new ASiCWithCAdESSignatureParameters();
        firstSignatureParameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_LTA);
        firstSignatureParameters.aSiC().setContainerType(ASiCContainerType.ASiC_E);
        firstSignatureParameters.bLevel().setSigningDate(new Date());

        secondSignatureParameters = new ASiCWithCAdESSignatureParameters();
        secondSignatureParameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_LTA);
        secondSignatureParameters.aSiC().setContainerType(ASiCContainerType.ASiC_E);
        secondSignatureParameters.bLevel().setSigningDate(new Date());
    }

    @Override
    protected DSSDocument getFirstSignedContainer() {
        SimpleASiCWithCAdESFilenameFactory filenameFactory = new SimpleASiCWithCAdESFilenameFactory();
        filenameFactory.setSignatureFilename("signatureAAA.p7s");
        filenameFactory.setTimestampFilename("timestampAAA.tst");
        filenameFactory.setManifestFilename("ASiCManifestAAA.xml");
        getService().setAsicFilenameFactory(filenameFactory);
        return super.getFirstSignedContainer();
    }

    @Override
    protected DSSDocument getSecondSignedContainer() {
        SimpleASiCWithCAdESFilenameFactory filenameFactory = new SimpleASiCWithCAdESFilenameFactory();
        filenameFactory.setSignatureFilename("signatureBBB.p7s");
        filenameFactory.setTimestampFilename("timestampBBB.tst");
        filenameFactory.setManifestFilename("ASiCManifestBBB.xml");
        getService().setAsicFilenameFactory(filenameFactory);
        return super.getSecondSignedContainer();
    }

    @Override
    protected ASiCContainerMerger getASiCContainerMerger(DSSDocument containerOne, DSSDocument containerTwo) {
        ASiCContainerMerger containerMerger = super.getASiCContainerMerger(containerOne, containerTwo);
        assertTrue(containerMerger instanceof ASiCEWithCAdESContainerMerger);
        ASiCEWithCAdESContainerMerger cadesContainerMerger = (ASiCEWithCAdESContainerMerger) containerMerger;

        SimpleASiCWithCAdESFilenameFactory filenameFactory = new SimpleASiCWithCAdESFilenameFactory();
        filenameFactory.setArchiveManifestFilename("ASiCArchiveManifestBBB.xml");
        cadesContainerMerger.setAsicFilenameFactory(filenameFactory);

        return cadesContainerMerger;
    }

    @Override
    protected void checkContainerInfo(DiagnosticData diagnosticData) {
        super.checkContainerInfo(diagnosticData);

        assertEquals(2, diagnosticData.getSignatures().size());
        boolean aaaSigFound = false;
        boolean bbbSigFound = false;
        for (SignatureWrapper signatureWrapper : diagnosticData.getSignatures()) {
            if ("META-INF/signatureAAA.p7s".equals(signatureWrapper.getSignatureFilename())) {
                aaaSigFound = true;
            } else if ("META-INF/signatureBBB.p7s".equals(signatureWrapper.getSignatureFilename())) {
                bbbSigFound = true;
            }
        }
        assertTrue(aaaSigFound);
        assertTrue(bbbSigFound);

        assertEquals(4, diagnosticData.getTimestampList().size());
        boolean aaaTstFound = false;
        boolean bbbTstFound = false;
        for (TimestampWrapper timestampWrapper : diagnosticData.getTimestampList()) {
            if (ArchiveTimestampType.CAdES_DETACHED.equals(timestampWrapper.getArchiveTimestampType())) {
                if ("META-INF/timestampAAA.tst".equals(timestampWrapper.getFilename())) {
                    aaaTstFound = true;
                } else if ("META-INF/timestampBBB.tst".equals(timestampWrapper.getFilename())) {
                    bbbTstFound = true;
                }
            }
        }
        assertTrue(aaaTstFound);
        assertTrue(bbbTstFound);

        assertEquals(4, diagnosticData.getContainerInfo().getManifestFiles().size());
        boolean aaaManifestFound = false;
        boolean bbbManifestFound = false;
        boolean firstArchiveManifestFound = false;
        boolean secondArchiveManifestFound = false;
        for (XmlManifestFile manifestFile : diagnosticData.getContainerInfo().getManifestFiles()) {
            if ("META-INF/ASiCManifestAAA.xml".equals(manifestFile.getFilename())) {
                aaaManifestFound = true;
            } else if ("META-INF/ASiCManifestBBB.xml".equals(manifestFile.getFilename())) {
                bbbManifestFound = true;
            } else if ("META-INF/ASiCArchiveManifest.xml".equals(manifestFile.getFilename())) {
                firstArchiveManifestFound = true;
            } else if ("META-INF/ASiCArchiveManifestBBB.xml".equals(manifestFile.getFilename())) {
                secondArchiveManifestFound = true;
            }
        }
        assertTrue(aaaManifestFound);
        assertTrue(bbbManifestFound);
        assertTrue(firstArchiveManifestFound);
        assertTrue(secondArchiveManifestFound);
    }

    @Override
    protected List<DSSDocument> getFirstSignedData() {
        return Collections.singletonList(documentToSign);
    }

    @Override
    protected List<DSSDocument> getSecondSignedData() {
        return Collections.singletonList(documentToSign);
    }

    @Override
    protected ASiCWithCAdESSignatureParameters getFirstSignatureParameters() {
        firstSignatureParameters.setSigningCertificate(getSigningCert());
        firstSignatureParameters.setCertificateChain(getCertificateChain());
        return firstSignatureParameters;
    }

    @Override
    protected ASiCWithCAdESSignatureParameters getSecondSignatureParameters() {
        secondSignatureParameters.setSigningCertificate(getSigningCert());
        secondSignatureParameters.setCertificateChain(getCertificateChain());
        return secondSignatureParameters;
    }

    @Override
    protected void checkSignatureLevel(DiagnosticData diagnosticData) {
        super.checkSignatureLevel(diagnosticData);

        for (SignatureWrapper signatureWrapper : diagnosticData.getSignatures()) {
            assertEquals(firstSignatureParameters.getSignatureLevel(), signatureWrapper.getSignatureFormat());
        }
    }

    @Override
    protected String getFirstSigningAlias() {
        return GOOD_USER;
    }

    @Override
    protected String getSecondSigningAlias() {
        return RSA_SHA3_USER;
    }

    @Override
    protected ASiCWithCAdESService getService() {
        return service;
    }

    @Override
    protected ASiCContainerType getExpectedASiCContainerType() {
        return ASiCContainerType.ASiC_E;
    }

}
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

public class Showcase {
    public static final String ORIGINAL_PATH = "c:/temp/merge-pdf/source.pdf";
    public static final String FILE_TO_EMBED = "c:/temp/merge-pdf/file-to-embed.pdf";
    public static final String OUTPUT_PATH_1 = "c:/temp/merge-pdf/result1.pdf";
    public static final String OUTPUT_PATH_2 = "c:/temp/merge-pdf/result2.pdf";

    public static void main(String[] args) throws IOException, DocumentException {
        mergeMethod1(ORIGINAL_PATH, FILE_TO_EMBED, OUTPUT_PATH_1, 1);
        mergeMethod2(ORIGINAL_PATH, FILE_TO_EMBED, OUTPUT_PATH_2);
    }

    /**
     * Method 1 from Stackoverflow
     *
     * @param originalFilePath PDF to append to
     * @param fileToInsertPath PDF to append
     * @param outputFile       Where to save to
     * @param location         where you want to insert the page
     * @see <a href="www.stackoverflow.com/questions/25872151/how-do-i-copy-a-pdf-into-another-without-losing-page-sizes-and-meta-information">Stackoverflow</a>
     */
    public static void mergeMethod1(String originalFilePath, String fileToInsertPath, String outputFile, int location) {
        PdfReader originalFileReader = null;
        try {
            originalFileReader = new PdfReader(originalFilePath);
        } catch (IOException ex) {
            System.out.println("ITextHelper.addPDFToPDF(): can't read original file: " + ex);
        }
        PdfReader fileToAddReader = null;
        try {
            fileToAddReader = new PdfReader(fileToInsertPath);
        } catch (IOException ex) {
            System.out.println("ITextHelper.addPDFToPDF(): can't read fileToInsert: " + ex);
        }

        if (originalFileReader != null && fileToAddReader != null) {

            // -- Copy
            int numberOfOriginalPages = originalFileReader.getNumberOfPages();
            Document document = new Document();
            PdfCopy copy = null;
            try {
                copy = new PdfCopy(document, new FileOutputStream(outputFile));
                document.open();

                for (int i = 1; i <= numberOfOriginalPages; i++) {
                    if (i == location) {
                        for (int j = 1; j <= fileToAddReader.getNumberOfPages(); j++) {
                            copy.addPage(copy.getImportedPage(fileToAddReader, j));
                        }
                    }
                    copy.addPage(copy.getImportedPage(originalFileReader, i));

                }
                document.close();
            } catch (DocumentException | IOException ex) {
                System.out.println("ITextHelper.addPDFToPDF(): can't read output location: " + ex);
            }
        }
    }

    /**
     * Method 2 from itext-docs
     *
     * @param outputFile
     * @throws IOException
     * @throws DocumentException
     * @see <a href="http://developers.itextpdf.com/examples/merging-pdf-documents-itext5/merging-documents-and-create-table-contents#795-mergewithtoc.java">Stackoverflow</a>
     */
    public static void mergeMethod2(String originalFilePath, String fileToInsertPath, String outputFile)
            throws IOException, DocumentException {

        Map<String, PdfReader> filesToMerge = new TreeMap<String, PdfReader>();
        filesToMerge.put("01 Hello World", new PdfReader(originalFilePath));
        filesToMerge.put("02 Movies / Countries", new PdfReader(fileToInsertPath));
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputFile));
        PdfCopy.PageStamp stamp;
        document.open();
        int n;
        int pageNo = 0;
        PdfImportedPage page;
        Chunk chunk;
        for (Map.Entry<String, PdfReader> entry : filesToMerge.entrySet()) {
            n = entry.getValue().getNumberOfPages();
            for (int i = 0; i < n; ) {
                pageNo++;
                page = copy.getImportedPage(entry.getValue(), ++i);
                stamp = copy.createPageStamp(page);
                chunk = new Chunk(String.format("Page %d", pageNo));
                if (i == 1)
                    chunk.setLocalDestination("p" + pageNo);
                ColumnText
                        .showTextAligned(stamp.getUnderContent(), Element.ALIGN_RIGHT, new Phrase(chunk), 559, 810, 0);
                stamp.alterContents();
                copy.addPage(page);
            }
        }
        document.close();
        for (PdfReader r : filesToMerge.values()) {
            r.close();
        }
    }

}

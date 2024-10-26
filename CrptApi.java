import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CrptApi {
    private static final String server_api_url="https://ismp.crpt.ru/api/v3/lk/documents/create";
    private int max_requests_limit;
    private long time_interval_micro_seconds;
    private volatile long start_of_interval_timestamp, number_of_requests_since_beginning_of_interval;
    public static class TimeUnit{
        public long value;
        public long unit;
        public TimeUnit(long value,long unit){
            this.value=value;
            this.unit=unit;
        }
        public long to_micro_seconds(){
            return value*unit;
        }
        public static final long second=1000000,micro_second=1
                ,milli_second=1000,minute=60*1000000,hour=3600*1000000;
    }
    public CrptApi(TimeUnit timeUnit, int requestLimit){
        time_interval_micro_seconds=timeUnit.to_micro_seconds();
        this.max_requests_limit =requestLimit;
    }
    public class Too_many_requests_exception extends Exception{

    }
    public void send_request(Document document,String signature)throws Too_many_requests_exception{
        if(start_of_interval_timestamp!=0){
            if((System.nanoTime()/1000)-start_of_interval_timestamp>time_interval_micro_seconds){
                start_of_interval_timestamp=System.nanoTime()/1000;
                number_of_requests_since_beginning_of_interval =0;
            }
            else{
                if(number_of_requests_since_beginning_of_interval>= max_requests_limit){
                    throw new Too_many_requests_exception();
                }
            }
        }else {
            start_of_interval_timestamp=System.nanoTime()/1000;
        }
        number_of_requests_since_beginning_of_interval++;
        if(number_of_requests_since_beginning_of_interval> max_requests_limit){
            throw new Too_many_requests_exception();
        }
        String json_encoded_document=json_encode(document);
        try {
            send_https_request_to_url(server_api_url,json_encoded_document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Gson json=new Gson();
    private String json_encode(Document document){
        return json.toJson(document);
    }
    private static void send_https_request_to_url(String url, String json) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK)
        {
            throw new IOException("HTTP error code: " + responseCode);
        }

        // Handle response (optional)
        // ...
    }

    public static class Document {
        public static class Description {
            public String participantInn;
        }
        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;

            // Constructor
            public Product(String certificateDocument, String certificateDocumentDate, String certificateDocumentNumber,
                           String ownerInn, String producerInn, String productionDate, String tnvedCode,
                           String uitCode, String uituCode) {
                this.certificate_document = certificateDocument;
                this.certificate_document_date = certificateDocumentDate;
                this.certificate_document_number = certificateDocumentNumber;
                this.owner_inn = ownerInn;
                this.producer_inn = producerInn;
                this.production_date = productionDate;
                this.tnved_code = tnvedCode;
                this.uit_code = uitCode;
                this.uitu_code = uituCode;
            }

            // Getters and Setters
            // (You may add getters and setters for each field here)
        }
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public List<Product> products;
        public String reg_date;
        public String reg_number;

        // Constructor
        public Document(String desc_participantInn,String docId, String docStatus, String docType, boolean importRequest,
                        String ownerInn, String participantInn, String producerInn, String productionDate,
                        String productionType, List<Product> products, String regDate, String regNumber) {
            this.description=new Description();
            this.description.participantInn=desc_participantInn;
            this.doc_id = docId;
            this.doc_status = docStatus;
            this.doc_type = docType;
            this.importRequest = importRequest;
            this.owner_inn = ownerInn;
            this.participant_inn = participantInn;
            this.producer_inn = producerInn;
            this.production_date = productionDate;
            this.production_type = productionType;
            this.products = products;
            this.reg_date = regDate;
            this.reg_number = regNumber;
        }
    }
}

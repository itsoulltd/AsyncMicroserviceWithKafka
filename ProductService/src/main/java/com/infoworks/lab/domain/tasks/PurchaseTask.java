package com.infoworks.lab.domain.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.ExecutableTask;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class PurchaseTask extends ExecutableTask<Message, Response> {

    public PurchaseTask() {}

    public PurchaseTask(SearchQuery query) {
        getMessage().setPayload(query.toString());
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //Call RestTemplate: api/payment/v1/checkout
        RestTemplate template = new RestTemplateBuilder()
                .rootUri("http://localhost:8092/api/payment")
                .build();
        try {
            SearchQuery purchase = MessageParser.unmarshal(SearchQuery.class, getMessage().getPayload());
            HttpEntity<SearchQuery> entity = new HttpEntity<>(purchase, new HttpHeaders());
            ResponseEntity<Response> res = template.exchange("/v1/checkout"
                    , HttpMethod.POST
                    , entity
                    , Response.class);
            return res.getBody();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.infoworks.lab.domain.tasks;

import com.infoworks.lab.beans.tasks.nuts.ExecutableTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class PurchaseTask extends ExecutableTask<Message, Response> {

    public PurchaseTask() {}

    public PurchaseTask(SearchQuery query) {
        getMessage().setEvent(query);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //Call RestTemplate: api/payment/v1/checkout
        RestTemplate template = new RestTemplateBuilder()
                .rootUri("http://localhost:8092/api/payment")
                .build();
        SearchQuery purchase = (SearchQuery) getMessage().getEvent(SearchQuery.class);
        purchase.add("purchase_task").isEqualTo(System.currentTimeMillis());
        //
        HttpEntity<SearchQuery> entity = new HttpEntity<>(purchase, new HttpHeaders());
        ResponseEntity<Response> res = template.exchange("/v1/checkout"
                , HttpMethod.POST
                , entity
                , Response.class);
        return res.getBody();
    }

}

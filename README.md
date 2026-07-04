#### How To Run
    
    # Start Kafka and Other tools:
    ~>$ docker-compose -f docker-compose-tools.yaml up -d
    ~>$ cd ProductService 
    ~>$ mvn clean spring-boot:run
    ~>$ cd ../PaymentService
    ~>$ mvn clean spring-boot:run
    ~>$ cd ../OrderService
    ~>$ mvn clean spring-boot:run
    ~>$ cd ../DeliveryDispatchService
    ~>$ mvn clean spring-boot:run
    # Stop all tools
    ~>$ docker-compose -f docker-compose-tools.yaml down
    
#### [Click to open /api/product](http://localhost:8091/api/product/swagger-ui.html)

#### Call Start via api-call to http://localhost:8091/api/product/v1/purchase/{product_id}

    ## Http curl:
    curl --location 'http://localhost:8091/api/product/v1/purchase/101' \
    --header 'Content-Type: application/json' \
    --data '{
        "descriptors": [
            {
                "keys": [],
                "order": "ASC"
            }
        ],
        "page": 0,
        "properties": [
            {
                "key": "product_name",
                "logic": "AND",
                "nextKey": null,
                "operator": "EQUAL",
                "type": "STRING",
                "value": "Biscuit & Cake"
            },
            {
                "key": "product_description",
                "logic": "AND",
                "nextKey": null,
                "operator": "EQUAL",
                "type": "STRING",
                "value": "Order For Biscuit & Cake"
            },
            {
                "key": "user-id",
                "logic": "AND",
                "nextKey": null,
                "operator": "EQUAL",
                "type": "STRING",
                "value": "001883"
            },
            {
                "key": "amount",
                "logic": "AND",
                "nextKey": null,
                "operator": "EQUAL",
                "type": "BIG_DECIMAL",
                "value": "120.00"
            }
        ],
        "size": 10
    }'
    
####

![Screenshot-01](/README.images/screenshot-01.png)
![Screenshot-01](/README.images/screenshot-02.png)
![Screenshot-01](/README.images/screenshot-03.png)
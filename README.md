####How To Run
    
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
    
####Call Start via API-Call

    ## Http curl:
    curl --location --request POST 'http://localhost:8091/api/product/v1/purchase' \
    --header 'Content-Type: application/json' \
    --header 'Accept: */*' \
    --data-raw '{
      "descriptors": [
        {
          "keys": [],
          "order": "ASC"
        }
      ],
      "page": 0,
      "properties": [
        {
          "key": "user-id",
          "logic": "AND",
          "nextKey": null,
          "operator": "EQUAL",
          "type": "STRING",
          "value": "001882"
        },
        {
          "key": "amount",
          "logic": "AND",
          "nextKey": null,
          "operator": "EQUAL",
          "type": "BIG_DECIMAL",
          "value": "100.00"
        }
      ],
      "size": 10
    }'
    
####
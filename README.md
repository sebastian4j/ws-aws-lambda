# AWS Lambda & WebSockets
Este repositorio contiene un ejemplo simple de comunicación utilizando web sockets desde aws lambdas y api gateway (se asume conocimiento previo en lambdas y api gateway de aws).

- Lo primero es descargar este proyecto y compilarlo para generar un jar.

- Luego hay que crear el lambda en aws y asociarlo con el jar generado.

- Despues crear en api gateway el api para nuestra conexion web socket:
> https://aws.amazon.com/es/blogs/compute/announcing-websocket-apis-in-amazon-api-gateway/

>https://docs.aws.amazon.com/es_es/apigateway/latest/developerguide/apigateway-websocket-api.html
>
> Recomiendo para este ejemplo utilizar en todos los **Routes** el mismo lambda
>
- Luego, al tener disponible el api, copiar desde la etapa creada el valor de **Connection URL** y escribirlo en las **Variables de entorno** del lambda asociado a la clave **url_api**:
> La Connection URL es algo como https://XXXXXXXX.execute-api.us-east-0.amazonaws.com/test/@connections

- Ahora que ya esta finalizado la prueba se puede realizar conectando a la **WebSocket URL** con:
> wscat -c wss://XXXXXXXX.execute-api.us-east-0.amazonaws.com/test

> El mensaje **connected (press CTRL+C to quit)** indica que estamos conectados y nos dice como salir

> El lambda responderá lo mismo que le escribamos

## Eso es todo :blush:


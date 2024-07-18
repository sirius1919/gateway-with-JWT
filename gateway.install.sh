# /bin/bash
docker build -t harbor.localhost.com/mycloud/gateway:latest .
docker login -u admin -p 1e3j5g7V harbor.localhost.com
docker push harbor.localhost.com/mycloud/gateway:latest
docker logout harbor.localhost.com
kubectl delete -f gateway.yaml
kubectl apply -f gateway.yaml

#!/bin/bash

docker container run -p5432:5432 -v$(pwd)/../docker/volumes/herbert_bot_db:/var/lib/postgresql/data  -e POSTGRES_DB=herbert_bot -e POSTGRES_USER=herbert -e POSTGRES_PASSWORD=botbasepassword -d postgres
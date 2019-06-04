#!/usr/bin/env bash

python manage.py migrate

python manage.py collectstatic --force

#Run daphne
daphne -b 0.0.0.0 -p 8001 cookbook_webservice.asgi:application

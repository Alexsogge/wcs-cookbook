FROM python:3

LABEL authors="Alexander Henkel <a.henkel@mgga.de>"
ENV PYTHONUNBUFFERED 1
RUN mkdir /code
WORKDIR /code
COPY requirements.txt /code/
RUN pip install -r requirements.txt
COPY . /code/
import threading
from random import randint

from flask import Blueprint, request
from jsonschema import validate, ValidationError
from tinydb import TinyDB, Query

exchange = Blueprint('exchange', __name__, template_folder='templates')
exchange_db = TinyDB('exchange_db.json')
exchange_db.purge()
exchange_db.insert({
    "name": "Bedrock",
    "stocks": 100,
    "currency": 500
})
exchange_schema = {
    "type": "object",
    "properties": {
        "stocks": {"type": "number"},
        "currency": {"type": "number"},
        "name": {"type": "string"}
    }
}
Company = Query()


@exchange.route('/exchange', methods=['GET'])
def get_all_exchange_data():
    return exchange_db.all(), 200


@exchange.route('/exchange', methods=['POST'])
def post_new_company():
    body = request.get_json()
    try:
        validate(body, schema=exchange_schema)
    except ValidationError:
        return "Incorrect json body", 400
    company_exchange_data = exchange_db.insert(body)
    return {'id': company_exchange_data}, 200


@exchange.route('/exchange/<company_name>', methods=['GET'])
def get_company_exchange_data(company_name):
    return exchange_db.get(Company.name == company_name), 200


@exchange.route('/exchange/<company_name>', methods=['PUT'])
def update_company_exchange_data(company_name):
    body = request.get_json()
    try:
        validate(body, schema=exchange_schema)
    except ValidationError:
        return "Incorrect json body", 400
    exchange_db.update(body, Company.name == company_name)
    return '', 200


@exchange.route('/exchange/<company_name>', methods=['DELETE'])
def delete_company_exchange_data(company_name):
    exchange_db.remove(Company.name == company_name)
    return '', 200


def change_currencies_a_bit():
    threading.Timer(60, change_currencies_a_bit).start()
    for item in exchange_db.all():
        exchange_db.update({'currency': randint(0, 1000)}, doc_ids=[item.doc_id])

import threading
from random import randint

from flask import Blueprint, Response, request

from model import Company

exchange = Blueprint('exchange', __name__, template_folder='templates')


@exchange.route('/exchange', methods=['GET'])
def get_all_exchange_data():
    exchange_data = Company.objects().to_json()
    return Response(exchange_data, mimetype="application/json", status=200)


@exchange.route('/exchange', methods=['POST'])
def post_new_company():
    body = request.get_json()
    company_exchange_data = Company(**body).save()
    new_id = company_exchange_data.id
    return {'id': str(new_id)}, 200


@exchange.route('/exchange/<company_id>', methods=['GET'])
def get_company_exchange_data(company_id):
    company_exchange_data = Company.objects.get(id=company_id).to_json()
    return Response(company_exchange_data, mimetype='application/json', status=200)


@exchange.route('/exchange/<company_id>', methods=['PUT'])
def update_company_exchange_data(company_id):
    body = request.get_json()
    Company.objects.get(id=company_id).update(**body)
    return '', 200


@exchange.route('/exchange/<company_id>', methods=['DELETE'])
def delete_company_exchange_data(company_id):
    Company.objects.get(id=company_id).delete()
    return '', 200


def change_currencies_a_bit():
    threading.Timer(60, change_currencies_a_bit).start()
    Company.objects().update(multi=True, currency=randint(0, 1000))

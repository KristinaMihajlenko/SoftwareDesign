from flask import Blueprint, abort, request, Response
from jsonschema import validate, ValidationError
from tinydb import TinyDB, Query

from exchange import exchange_db, Company

user = Blueprint('profile', __name__, template_folder='templates')
users_db = TinyDB('users_db.json')
users_db.purge()
shares_db = TinyDB('shares_db.json')
shares_db.purge()
user_schema = {
    'type': 'object',
    'properties': {
        'name': {'type': 'string'},
        'amount': {'type': 'number'}
    }
}
shares_schema = {
    'type': 'object',
    'properties': {
        'company_name': {'type': 'string'},
        'number': {'type': 'number'}
    }
}
User = Query()
Share = Query()
users_db.insert(dict(name="Alexander", amount=10000))
shares_db.insert(dict(company_name="Bedrock", user_name="Alexander", number=10))


@user.route('/users', methods=['GET'])
def get_all_users():
    return users_db.all(), 200


@user.route('/users', methods=['POST'])
def create_new_user():
    body = request.get_json()
    try:
        validate(body, schema=user_schema)
    except ValidationError:
        return "Incorrect json body", 400
    if users_db.get(User.name == body['name']) is not None:
        return "User with equal name already exists"
    this_user = users_db.insert(body)
    return {'id': this_user}, 200


@user.route('/users/<user_name>', methods=['GET'])
def get_user_data(user_name):
    return users_db.get(User.name == user_name), 200


@user.route('/users/<user_name>', methods=['PUT'])
def update_user_data(user_name):
    body = request.get_json()
    try:
        validate(body, schema=user_schema)
    except ValidationError:
        return "Incorrect json body", 400
    users_db.update(body, User.name == user_name)
    return '', 200


@user.route('/users/<user_name>', methods=['DELETE'])
def delete_user(user_name):
    users_db.remove(User.name == user_name)
    shares_db.remove(Share.user_name == user_name)
    return 'User and his shares deleted', 200


@user.route('/users/<user_name>/shares', methods=['GET'])
def get_user_shares(user_name):
    return shares_db.search(Share.user_name == user_name), 200


@user.route('/users/<user_name>/shares/buy', methods=['POST'])
def buy_shares(user_name):
    body = request.get_json()
    current_user = users_db.get(User.name == user_name)
    if current_user is None:
        Response("There's no such User", mimetype='plain/text', status=404)
    for item in body:
        try:
            validate(item, schema=shares_schema)
        except ValidationError:
            return Response("Validate failed", mimetype='plain/text', status=400)
        if item['number'] <= 0:
            return Response("", mimetype='plain/text', status=400)
        company = exchange_db.get(Company.name == item['company_name'])
        if company is None:
            return Response("There's no such Company", mimetype='plain/text', status=400)

        if current_user['amount'] >= company['currency'] * item['number']:
            if item['number'] <= company['stocks']:
                users_db.update(dict(amount=current_user['amount'] - company['currency'] * item['number']),
                                User.name == user_name)
                share = shares_db.get((User.user_name == user_name) & (Company.company_name == company['name']))
                new_number = share['number'] + item['number']
                shares_db.upsert(dict(company_name=company['name'], user_name=user_name, number=new_number),
                                 (User.user_name == user_name) & (Company.company_name == company['name']))
                exchange_db.update(dict(stocks=company['stocks']-item['number']))
            else:
                return Response(company['name'] + ' shares number less than you request to offer',
                                mimetype='plain/text', status=400)
        else:
            return Response("Not enough money to afford", mimetype='plain/text', status=400)
            # return 'Not enough money to afford', 400
    return Response("Success", mimetype='plain/text', status=200)


@user.route('/users/<user_name>/shares/sell', methods=['POST'])
def sell_shares(user_name):
    body = request.get_json()
    current_user = users_db.get(User.name == user_name)
    if current_user is None:
        abort(404)
        return "User not found"

    for item in body:
        try:
            validate(item, schema=shares_schema)
        except ValidationError:
            return Response("Validate failed", mimetype='plain/text', status=400)
        if item['number'] <= 0: return "Incorrect shares number", 400
        company = exchange_db.get(Company.name == item['company_name'])
        if company is None: return "There is no such Company", 400
        user_share = shares_db.get((User.user_name == user_name) & (Company.company_name == company['name']))
        if user_share is None: return "User don't have any shares of that Company", 400
        if item['number'] < user_share['number']:
            users_db.update(dict(amount=current_user['amount'] + company['currency'] * item['number']),
                            User.name == user_name)
            shares_db.update(dict(number=user_share['number'] - item['number']),
                             (User.user_name == user_name) & (Company.company_name == company['name']))
            exchange_db.update(dict(stocks=company['stocks'] + item['number']))
        elif item['number'] == user_share['number']:
            users_db.update(dict(amount=current_user['amount'] + company['currency'] * item['number']),
                            User.name == user_name)
            shares_db.remove((User.user_name == user_name) & (Company.company_name == company['name']))
            exchange_db.update(dict(stocks=company['stocks'] + item['number']))
        else:
            return "User doesn't have enough shares", 400
    return 'Success', 200


@user.route('/users/<user_name>/shares/all-value', methods=['GET'])
def current_value_all_shares(user_name):
    current_user = users_db.get(User.name == user_name)
    if current_user is None:
        abort(404)
        return "User not found"
    user_shares = shares_db.search(Share.user_name == user_name)
    shares_amount = 0 + current_user['amount']
    for item in user_shares:
        shares_amount += exchange_db.get(Company.name == item['company_name'])['currency'] * item['number']
    return "The user's assets in cash equivalent at the moment: " + str(shares_amount), 200

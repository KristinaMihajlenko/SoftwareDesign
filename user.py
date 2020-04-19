from flask import Blueprint, abort, Response, request
from mongoengine import DoesNotExist

from model import User, Shares, Company

user = Blueprint('profile', __name__, template_folder='templates')


@user.route('/users', methods=['GET'])
def get_all_users():
    return Response(User.objects.to_json(), mimetype='application/json', status=200)


@user.route('/users', methods=['POST'])
def create_new_user():
    body = request.get_json()
    user_data = User(**body).save()
    new_id = user_data.id
    return {'id': str(new_id)}, 200


@user.route('/users/<user_id>', methods=['GET'])
def get_user_data(user_id):
    user_data = User.objects.get(id=user_id).to_json()
    return Response(user_data, mimetype='application/json', status=200)


@user.route('/users/<user_id>', methods=['PUT'])
def update_user_data(user_id):
    body = request.get_json()
    User.objects.get(id=user_id).update(**body)
    return '', 200


@user.route('/users/<user_id>', methods=['DELETE'])
def delete_user(user_id):
    current_user = User.objects.get(id=user_id)
    try:
        Shares.objects.get(user=current_user).delete()
    except DoesNotExist:
        pass
    current_user.delete()
    return 'User and his shares deleted', 200


@user.route('/users/<user_id>/shares', methods=['GET'])
def get_user_shares(user_id):
    current_user = User.objects(id=user_id).get()
    user_shares = Shares.objects(user=current_user).all().to_json()
    return Response(user_shares, mimetype='application/json', status=200)


@user.route('/users/<user_id>/shares/buy', methods=['POST'])
def buy_shares(user_id):
    body = request.get_json()
    try:
        current_user = User.objects(id=user_id).get()
    except DoesNotExist:
        abort(404)
        return "User not found"
    for item in body.shares:
        if item['number'] <= 0:
            continue
        try:
            company = Company.objects(id=item['company_id']).get()
        except DoesNotExist:
            continue
        company_stocks_number = company['stocks']
        current_user_amount = current_user['amount']
        company_currency = company['currency']
        if current_user_amount >= company_currency * item['number']:
            if item['number'] <= company_stocks_number:
                current_user.update(amount=current_user_amount - company_currency * item['number'])
                try:
                    new_number = Shares.objects(user=current_user, company=company).get()['number'] + item[
                        'number']
                    Shares.objects(user=current_user, company=company).update(number=new_number)
                except DoesNotExist:
                    Shares(company=company, user=current_user, number=item['number']).save()
            else:
                abort(409)
        else:
            abort(409)
    return 'Success', 200


@user.route('/users/<user_id>/shares/sell', methods=['POST'])
def sell_shares(user_id):
    body = request.get_json()
    try:
        current_user = User.objects(id=user_id).get()
    except DoesNotExist:
        abort(404)
        return "User not found"
    current_user_amount = current_user['amount']
    for item in body.shares:
        if item['number'] <= 0:
            continue
        try:
            company = Company.objects(id=item['company_id']).get()
        except DoesNotExist:
            continue
        try:
            user_share = Shares.objects(user=current_user, company=company).get()
        except DoesNotExist:
            continue
        company_currency = company['currency']
        # Проверить, достаточно ли акций этой компании на счету у пользователя
        if item['number'] < user_share['number']:
            current_user.update(amount=current_user_amount + company_currency * item['number'])
            user_share.update(number=user_share['number'] - item['number'])
        elif item['number'] == user_share['number']:
            current_user.update(amount=current_user_amount + company_currency * item['number'])
            user_share.delete()
        else:
            abort(409)
    return 'Success', 200


@user.route('/users/<user_id>/shares/all-value', methods=['GET'])
def current_value_all_shares(user_id):
    try:
        current_user = User.objects(id=user_id).get()
    except DoesNotExist:
        abort(404)
        return "User not found"
    current_user_amount = current_user['amount']
    user_shares = Shares.objects(user=current_user).all().select_related()
    shares_amount = 0 + current_user_amount
    for item in user_shares:
        shares_amount += item['company']['currency'] * item['number']
    return "The user's assets in cash equivalent at the moment: " + str(shares_amount), 200

from db import db


class User(db.Document):
    name = db.StringField(max_length=100, required=True)
    amount = db.IntField(default=0, min_value=0)


class Company(db.Document):
    name = db.StringField(max_length=250, required=True)
    stocks = db.IntField(default=0, min_value=0)
    currency = db.IntField(default=0)


class Shares(db.Document):
    user = db.ReferenceField(User, required=True)
    company = db.ReferenceField(Company, required=True)
    number = db.IntField(default=0, min_value=0)

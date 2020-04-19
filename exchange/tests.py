import unittest

from app import app
from db import db


class MyTestCase(unittest.TestCase):
    def setUp(self):
        self.app = app.test_client()
        self.db = db.get_db()

    def test(self):
        user_payload = {
            "name": "Alexander",
            "amount": 10000
        }
        company_payload = {
            "name": "Bedrock inc.",
            "stocks": 100,
            "currency": 500
        }
        response = self.app.post('/users', headers={"Content-Type": "application/json"}, data=user_payload)
        self.assertEqual(200, response.status_code)

        response = self.app.post('/exchange', headers={"Content-Type": "application/json"}, data=company_payload)
        self.assertEqual(200, response.status_code)


    def tearDown(self):
        for collection in self.db.list_collection_names():
            self.db.drop_collection(collection)


if __name__ == '__main__':
    unittest.main()

import unittest

from app import app


class MyTestCase(unittest.TestCase):
    def setUp(self):
        self.app = app.test_client()

        user_payload = dict(name="Fill", amount=1000)
        company_payload = dict(name="Alphabet", stocks=100, currency=500)
        self.app.post('/users', headers={"Content-Type": "application/json"}, data=user_payload)
        self.app.post('/exchange', headers={"Content-Type": "application/json"}, data=company_payload)

    def test_buy_and_sell_shares(self):
        payload = dict(company_name="Alphabet", number=1)
        response = self.app.post('/users/Fill/shares/buy', headers={"Content-Type": "application/json"}, data=payload)
        self.assertEqual(response.status_code, 200, msg=response.get_data(as_text=True))

        payload = dict(company_name="Alphabet", number=1)
        response = self.app.post('/users/Fill/shares/sell', headers={"Content-Type": "application/json"}, data=payload)
        self.assertEqual(response.status_code, 200, msg=response.get_data(as_text=True))

    def test_company_doesnt_exists(self):
        payload = dict(company_name="Umbrella", number=1)
        response = self.app.post('/users/Fill/shares/buy', headers={"Content-Type": "application/json"}, data=payload)
        self.assertEqual(response.status_code, 400)

    def test_not_enough_shares(self):
        payload = dict(company_name="Alphabet", number=1)
        response = self.app.post('/users/Fill/shares/sell', headers={"Content-Type": "application/json"}, data=payload)
        self.assertEqual(response.status_code, 400)

    def tearDown(self):
        pass


if __name__ == '__main__':
    unittest.main()

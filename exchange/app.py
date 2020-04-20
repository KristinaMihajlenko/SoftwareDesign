from flask import Flask

from exchange import exchange, change_currencies_a_bit

from user import user

app = Flask(__name__)

app.register_blueprint(exchange)
app.register_blueprint(user)

change_currencies_a_bit()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')

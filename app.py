from flask import Flask

from db import initialize_db
from exchange import exchange, change_currencies_a_bit

from user import user

app = Flask(__name__)
app.config['MONGODB_SETTINGS'] = {
    'host': 'mongodb://localhost/exchange',
}
initialize_db(app)

app.register_blueprint(exchange)
app.register_blueprint(user)

change_currencies_a_bit()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')

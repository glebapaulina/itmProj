from flask import request, Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity
from project.db import mongo
from project.models.dto import WynikOperacji
from project.utils import response, podajUzytkownikaPoId

tests_blueprint = Blueprint(
    'tests',
    __name__
)


@tests_blueprint.route('/buttonTest', methods=["POST"])
@jwt_required
def dodajWynikButtontestu():
    wynikTestu = request.json #pobranie danych ��dania od androida - pobrane dane maj� struktur� s�ownikow� (mapa)
    print('headers: ' + str(request.headers)) #wy�wietlenie w konsoli danych wej�ciowych z �adania
    print('REQUEST ' + str(wynikTestu)) #wy�wietlenie w konsoli danych wej�ciowych z �adania

    # skoro jest to s�ownik, mo�emy odwo�a� si� do warto�ci poprzez mechanizm slownik['klucz'] -> warto��.
    # w tym przypadku przypisujemy do klucza 'test' warto�� 'buttonTest', by w bazie danych by�o wiadomo kt�ry to by� test.
    wynikTestu['test'] = 'buttonTest'

    user_id = get_jwt_identity() #dzi�ki u�yciu ma�pki @jwt_required mo�emy u�y� tej metody by wy�uska� id u�ytkownika kt�ry wys�a� ��danie.
    user = podajUzytkownikaPoId(user_id) #metoda w project/utils zwracaj�ca obiekt u�ytkownika po jego id.

    wynikTestu['user_id'] = user_id # przypisanie do wyniku testu id u�ytkownika

    print('Dane uzytkownika pobranego z bazy przy pomocy jwt tokena: ' + str(user)) #wyswietlenie kontrolne danych w konsoli

    #przypisania aktualnych danych uzytkownika do wyniku testu:
    wynikTestu['waga'] = user['waga']
    wynikTestu['wiek'] = user['wiek']
    wynikTestu['wzrost'] = user['wzrost']

    mongo.db.buttonTest.insert_one(wynikTestu) #wykonanie insertu do bazy danych

    # zwracamy dane uzywajac metody response() z project/utils. Metoda ta zamienia ka�dy obiekt z pythona na JSON.
    # jest to potrzebne, by android m�g� w poprawny spos�b otrzyma� dane.
    # obiekt kt�ry zwracamy, to WynikOperacji z project/models/dto.py kt�ry pozwala na zdefiniowanie identyfikatora oraz informacji boolowskiej.
    # po stronie androida musi by� utworzona klasa implementuj�ca w analogiczny spos�b obiekt jak ten po stronie pythona.
    return response(WynikOperacji("buttonTestWynik", True))


@tests_blueprint.route('/ninjaTest', methods=["POST"])
@jwt_required
def dodajWynikNinjatestu():
    wynikTestu = request.json
    print('headers: ' + str(request.headers))
    print('REQUEST ' + str(wynikTestu))

    wynikTestu['test'] = 'ninjaTest'

    user_id = get_jwt_identity()
    user = podajUzytkownikaPoId(user_id)

    wynikTestu['user_id'] = user_id

    print('Dane uzytkownika pobranego z bazy przy pomocy jwt tokena: ' + str(user))

    wynikTestu['waga'] = user['waga']
    wynikTestu['wiek'] = user['wiek']
    wynikTestu['wzrost'] = user['wzrost']

    mongo.db.buttonTest.insert_one(wynikTestu)

    wynikTestu['jakasWartosc'] = 5

    mongo.db.testowaTabela.insert_one(wynikTestu)

    return response('OK')
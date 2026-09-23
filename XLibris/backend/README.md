# XLibris
## Опис бізнес-правил та матриці переходів станів
### BookRequest
* Користувач не може створити BookRequest на свою ж книгу
* Користувач не може створити BookRequest на книгу в статусі BLOCKED
* Користувач не може створити BookRequest на книгу, якщо в нього вже є існуючий Bookrequest для цієї книги
* Не можна видалити неіснуючий BookRequest
* Якщо власник переводить книгу в статус BLOCKED, всі BookRequest на неї перезодять в статус CANCELLED
* Позичальник може підтвердити/скасувати BookRequest тільки після підтвердження власника

**Матриця переходів**
- PENDING -> APPROVED
- PENDING -> REJECTED
- PENDING -> CANCELLED
- APPROVED -> FULFILLED
- APPROVED -> REJECTED
- APPROVED -> CANCELED

### User
* Не можна побачити контактну інформацію користувача, якщо у вас з ним немає спільного Loan
* Лише адміністратор може редагувати рейтинги користувача
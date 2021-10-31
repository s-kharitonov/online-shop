--liquibase formatted sql
--changeset skharitonov:2021-09-22-init-products-01
insert into products (price, creation_date, last_update_date, version)
values (135990, current_timestamp, current_timestamp, 0);
insert into product_descriptions (product_id, language_id, title, description, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'),
        'Ноутбук Apple MacBook Pro 13 Late 2020',
        'С появлением чипа M1 MacBook Pro 13 дюймов становится невероятно производительным и быстрым. Мощность центрального процессора выросла до 2,8 раза. Скорость обработки графики — до 5 раз. Благодаря нашей передовой системе Neural Engine скорость машинного обучения возросла до 11 раз. При этом MacBook Pro работает без подзарядки до 20 часов — дольше, чем любой другой Mac. Наш самый популярный ноутбук класса Pro выходит на совершенно новый уровень. Встречайте. Наш первый чип, разработанный специально для Mac. Поразительно, но система на чипе Apple M1 вмещает 16 миллиардов транзисторов и объединяет центральный и графический процессоры, систему Neural Engine, контроллеры ввода-вывода и множество других компонентов. Чип Apple M1 позволяет использовать на Mac уникальные технологии и обеспечивает невероятную производительность в сочетании с лучшей в отрасли энергоэффективностью. Это не просто ещё один шаг для Mac — это принципиально новый уровень возможностей. 8‑ядерный графический процессор в чипе M1 — самый мощный из всех, когда-либо созданных Apple. И самый быстрый в мире интегрированный графический процессор для персонального компьютера. Благодаря ему скорость обработки графики выросла в 5 раз. Операционная система macOS Big Sur разработана с расчётом на огромный потенциал чипа M1. Значительно обновлённые приложения. Стильный новый дизайн. И передовые функции безопасности и защиты данных. Новая операционная система — это невероятно мощное программное обеспечение, разработанное для нашего самого продвинутого оборудования.',
        current_timestamp, current_timestamp, 0);
insert into product_descriptions (product_id, language_id, title, description, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'),
        'Apple MacBook Pro 13 Late 2020 laptop',
        'With the introduction of the M1 chip, the 13-inch MacBook Pro is incredibly powerful and fast. The power of the central processor has increased up to 2.8 times. Graphics processing speed - up to 5 times. With our advanced Neural Engine, machine learning is up to 11x faster. And MacBook Pro lasts up to 20 hours on a single charge - longer than any other Mac. Our most popular Pro laptop takes it to a whole new level. Meet. Our first chip designed specifically for the Mac. Amazingly, Apple''s M1-based system houses 16 billion transistors and integrates the CPU, GPU, Neural Engine, I / O controllers, and a host of other components. The Apple M1 chip brings unique technology to your Mac and delivers incredible performance with industry-leading energy efficiency. This isn''t just another step for the Mac - it''s a whole new level of capabilities. The 8-core GPU in the M1 chip is the most powerful Apple has ever built. And the world''s fastest integrated graphics processor for a personal computer. Thanks to him, the graphics processing speed has increased 5 times. MacOS Big Sur is designed with the M1 chip in mind. Significantly updated apps. Stylish new design. And advanced security and data protection features. The new operating system is incredibly powerful software designed for our most advanced hardware.',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'экран', '13.3 (2560x1600) IPS',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'процессор',
        'Apple M1 (8x3200 МГц)', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'память',
        'RAM 8 ГБ, SSD 512 ГБ', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'видеокарта',
        'встроенная, Apple graphics 8-core', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'screen',
        '13.3 (2560x1600) IPS', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'CPU', 'Apple M1 (8x3200 МГц)',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'memory',
        'RAM 8 ГБ, SSD 512 ГБ', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'GPU',
        'embedded, Apple graphics 8-core', current_timestamp, current_timestamp, 0);
--rollback delete from products p where p.id = currval('products_id_seq');

--changeset skharitonov:2021-09-22-init-products-02
insert into products (price, creation_date, last_update_date, version)
values (72100, current_timestamp, current_timestamp, 0);
insert into product_descriptions (product_id, language_id, title, description, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'),
        'Ноутбук ASUS Zenbook 13 UX325EA-KG285T',
        'Новый ноутбук ZenBook 13 OLED стал еще более тонким (13,9 мм) и легким (1,14 кг), а значит и еще более мобильным, при этом он предлагает передовой дисплей с великолепным качеством изображения и полноценный комплект интерфейсов, включающий HDMI, Thunderbolt 4 (USB-C), USB-A и microSD. Обладая высокопроизводительной конфигурацией, ZenBook 13 OLED представляет собой идеальный выбор для всех, кто постоянно находится в движении.',
        current_timestamp, current_timestamp, 0);
insert into product_descriptions (product_id, language_id, title, description, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'),
        'Laptop ASUS Zenbook 13 UX325EA-KG285T',
        'The new ZenBook 13 OLED laptop is thinner (13.9mm), lighter (1.14kg), and therefore even more mobile, while it offers an advanced display with stunning picture quality and a complete set of interfaces including HDMI, Thunderbolt 4 (USB-C), USB-A and microSD. With a high-performance configuration, ZenBook 13 OLED is the perfect choice for anyone on the go./ O controllers, and a host of other components. The Apple M1 chip brings unique technology to your Mac and delivers incredible performance with industry-leading energy efficiency. This isn''t just another step for the Mac - it''s a whole new level of capabilities. The 8-core GPU in the M1 chip is the most powerful Apple has ever built. And the world''s fastest integrated graphics processor for a personal computer. Thanks to him, the graphics processing speed has increased 5 times. MacOS Big Sur is designed with the M1 chip in mind. Significantly updated apps. Stylish new design. And advanced security and data protection features. The new operating system is incredibly powerful software designed for our most advanced hardware.',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'экран',
        '13.3 (1920x1080) OLED',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'процессор',
        'Intel Core i5 1135G7 (4x2400 МГц)', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'память',
        'RAM 16 ГБ (4266 МГц), SSD 512 ГБ', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'RU'), 'видеокарта',
        'встроенная, Intel Iris Xe Graphics', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'screen',
        '13.3 (1920x1080) OLED', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'CPU',
        'Intel Core i5 1135G7 (4x2400 МГц)',
        current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'memory',
        'RAM 16 ГБ (4266 МГц), SSD 512 ГБ', current_timestamp, current_timestamp, 0);
insert into product_features (product_id, language_id, name, value, creation_date, last_update_date, version)
values (currval('products_id_seq'), (select l.id from languages l where l.code = 'EN'), 'GPU',
        'embedded, Intel Iris Xe Graphics', current_timestamp, current_timestamp, 0);
--rollback delete from products p where p.id = currval('products_id_seq');
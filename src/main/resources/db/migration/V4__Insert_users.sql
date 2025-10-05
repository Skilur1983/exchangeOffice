SET search_path = office;

INSERT INTO office.users (username, password, role)
VALUES
    ('admin_first', crypt('admin123password', gen_salt('bf')), 'ADMIN'),
    ('admin_second', crypt('admin123password', gen_salt('bf')), 'ADMIN');

INSERT INTO office.users (username, password, role)
VALUES
    ('SarSmi', crypt('customer123', gen_salt('bf')), 'CUSTOMER'),
    ('TomBro', crypt('customer123', gen_salt('bf')), 'CUSTOMER');
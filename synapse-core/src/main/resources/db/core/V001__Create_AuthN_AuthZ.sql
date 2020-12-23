CREATE TABLE users (
  user_id BIGSERIAL NOT NULL,
  email varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
  enabled boolean DEFAULT FALSE,
  PRIMARY KEY (user_id)
);

CREATE TABLE roles (
  role_id BIGSERIAL NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (role_id)
);

CREATE TABLE users_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id,role_id),
  CONSTRAINT fk_user_role_userid FOREIGN KEY (user_id) REFERENCES users (user_id),
  CONSTRAINT fk_user_role_roleid FOREIGN KEY (role_id) REFERENCES roles (role_id)
);

CREATE TABLE privileges (
  privilege_id BIGSERIAL NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (privilege_id)
);

CREATE TABLE roles_privs (
  role_id BIGINT NOT NULL,
  privilege_id BIGINT NOT NULL,
  PRIMARY KEY (role_id,privilege_id),
  CONSTRAINT fk_role_privs_roleid FOREIGN KEY (role_id) REFERENCES roles (role_id),
  CONSTRAINT fk_role_privs_privilegeid FOREIGN KEY (privilege_id) REFERENCES privileges (privilege_id)
);


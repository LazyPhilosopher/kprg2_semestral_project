-- -- Microsoft SQL Server;
--
-- -- Creating the USERS table if it does not exist
-- IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='USERS')
-- BEGIN
-- CREATE TABLE USERS (
--                        ID BIGINT PRIMARY KEY IDENTITY(1,1),
--                        PASSWORD NVARCHAR(255) NOT NULL,
--                        ROLE NVARCHAR(255),
--                        USERNAME NVARCHAR(255) NOT NULL
-- )
-- END;
--
--
-- INSERT INTO USERS (PASSWORD, ROLE, USERNAME) VALUES ('1234', 'Admin', 'root');
-- INSERT INTO USERS (PASSWORD, ROLE, USERNAME) VALUES ('invicta', 'Admin', 'roma');



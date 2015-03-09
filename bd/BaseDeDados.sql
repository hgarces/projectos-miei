DROP TABLE eventos CASCADE CONSTRAINTS;
CREATE TABLE eventos (
	ID_evento number (8) NOT NULL,
	nomeFest varchar2 (30) NOT NULL,
	local varchar2 (40) NOT NULL,
	data date NOT NULL,
	numBilhetes number (5) NOT NULL,
	PRIMARY KEY (ID_evento)
);

DROP TABLE concertos CASCADE CONSTRAINTS;
CREATE TABLE concertos (
	ID_evento number (8) NOT NULL,
	PRIMARY KEY (ID_evento),
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE
);

DROP TABLE festivais CASCADE CONSTRAINTS;
CREATE TABLE festivais (
	ID_evento number (8) NOT NULL,
	numDias number (3) NOT NULL,
	PRIMARY KEY (ID_evento),
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE
);

DROP TABLE empresas CASCADE CONSTRAINTS;
CREATE TABLE empresas (
	ID_empresa number (8) NOT NULL,
	nomeEmp varchar2 (40),
	sede varchar2 (100),
	telefone varchar2 (12),
	PRIMARY KEY (ID_empresa)
);

DROP TABLE postos_venda CASCADE CONSTRAINTS;
CREATE TABLE postos_venda (
	ID_empresa number (8) NOT NULL,
	PRIMARY KEY (ID_empresa),
	FOREIGN KEY (ID_empresa) REFERENCES empresas ON DELETE CASCADE
);

DROP TABLE cargo CASCADE CONSTRAINTS;
CREATE TABLE cargo (
	ID_cargo number (8) NOT NULL,
	descric varchar2 (25) NOT NULL,
	PRIMARY KEY (ID_cargo)
);

DROP TABLE pessoas CASCADE CONSTRAINTS;
CREATE TABLE pessoas (
	nrBI number (10) NOT NULL,
	nomeP varchar2 (40) NOT NULL,
	telefone varchar2 (12) NOT NULL,
	email varchar2 (35) NOT NULL,
	PRIMARY KEY (email)
);

DROP TABLE staff CASCADE CONSTRAINTS;
CREATE TABLE staff (
	email varchar2 (30) NOT NULL,
	ID_cargo number (8) NOT NULL,
	PRIMARY KEY (email),
	FOREIGN KEY (email) REFERENCES pessoas ON DELETE CASCADE,
	FOREIGN KEY (ID_cargo) REFERENCES cargo ON DELETE CASCADE
);

DROP TABLE artistas CASCADE CONSTRAINTS;
CREATE TABLE artistas (
	email varchar2 (30) NOT NULL,
	PRIMARY KEY (email),
	FOREIGN KEY (email) REFERENCES pessoas ON DELETE CASCADE
);

DROP TABLE agentes CASCADE CONSTRAINTS;
CREATE TABLE agentes (
	email varchar2 (30) NOT NULL,	
	PRIMARY KEY (email),
	FOREIGN KEY (email) REFERENCES pessoas ON DELETE CASCADE
);

DROP TABLE bandas CASCADE CONSTRAINTS;
CREATE TABLE bandas (
	ID_banda number (8) NOT NULL,
	nomeB varchar2 (40) NOT NULL,
	pais varchar2 (30) NOT NULL,
	ID_agente varchar2 (30),
	PRIMARY KEY(ID_banda),
	FOREIGN KEY (ID_agente) REFERENCES agentes ON DELETE CASCADE
);

DROP TABLE eventos_empresas CASCADE CONSTRAINTS;
CREATE TABLE eventos_empresas (
	valor number (10) NOT NULL,
	ID_evento number (8) NOT NULL, 
	ID_empresa number (8) NOT NULL,
	PRIMARY KEY (ID_evento, ID_empresa),
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE,
	FOREIGN KEY (ID_empresa) REFERENCES empresas ON DELETE CASCADE
);

DROP TABLE bandas_eventos CASCADE CONSTRAINTS;
CREATE TABLE bandas_eventos (
	cachet number (10) NOT NULL,
	ID_evento number(8) NOT NULL, 
	ID_banda number (8) NOT NULL,
	PRIMARY KEY (ID_banda, ID_evento),
	FOREIGN KEY (ID_banda) REFERENCES bandas ON DELETE CASCADE,
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE
);

DROP TABLE venda_bilhetes CASCADE CONSTRAINTS;
CREATE TABLE venda_bilhetes (
	precoBilh number (8) NOT NULL,
	bVendidos number (8) NOT NULL,
	ID_evento number (8) NOT NULL, 
	ID_empresa number (8) NOT NULL,
	PRIMARY KEY (ID_empresa, ID_evento),
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE,
	FOREIGN KEY (ID_empresa) REFERENCES postos_venda ON DELETE CASCADE
);

DROP TABLE bandas_artistas CASCADE CONSTRAINTS;
CREATE TABLE bandas_artistas (
	ID_banda number (8) NOT NULL,
	ID_artista varchar (30) NOT NULL,
	PRIMARY KEY (ID_banda, ID_artista),
	FOREIGN KEY (ID_banda) REFERENCES bandas ON DELETE CASCADE,
	FOREIGN KEY (ID_artista) REFERENCES artistas ON DELETE CASCADE
);

DROP TABLE staff_eventos CASCADE CONSTRAINTS;
CREATE TABLE staff_eventos (
	ID_evento number(8) NOT NULL, 
	ID_staff varchar2 (30) NOT NULL,
	PRIMARY KEY (ID_staff, ID_evento),
	FOREIGN KEY (ID_staff) REFERENCES staff ON DELETE CASCADE,
	FOREIGN KEY (ID_evento) REFERENCES eventos ON DELETE CASCADE
);

DROP SEQUENCE seq_evento;
CREATE SEQUENCE seq_evento INCREMENT BY 1 START WITH 1 ;
DROP SEQUENCE seq_empresa;
CREATE SEQUENCE seq_empresa INCREMENT BY 1 START WITH 1 ;
DROP SEQUENCE seq_banda;
CREATE SEQUENCE seq_banda INCREMENT BY 1 START WITH 1 ;
DROP SEQUENCE seq_cargo;
CREATE SEQUENCE seq_cargo INCREMENT BY 1 START WITH 1 ;

CREATE OR REPLACE FORCE VIEW  "V_AGENTES" ("NRBI", "NOMEP", "TELEFONE", "EMAIL") AS 
  select nrBI, nomeP, telefone, email
from agentes natural inner join pessoas
/

CREATE OR REPLACE FORCE VIEW  "V_ARTISTAS" ("NRBI", "NOMEP", "TELEFONE", "EMAIL") AS 
  select nrBI, 
    nomeP, 
    telefone,
    email
from artistas natural inner join pessoas
/

CREATE OR REPLACE FORCE VIEW  "V_ARTISTAS_BANDA" ("NRBI", "NOMEP", "TELEFONE", "EMAIL", "ID_BANDA") AS 
  select p.nrbi, p.nomep, p.telefone, p.email, ba.id_banda
from artistas a, pessoas p, bandas_artistas ba
where a.email = p.email and p.email = ba.ID_artista
/

CREATE OR REPLACE FORCE VIEW  "V_BANDAS_EVENTOS" ("PK", "ID_BANDA", "CACHET", "ID_EVENTO") AS 
  select ROWID pk, id_banda, cachet, id_evento
from bandas_eventos
/
​
CREATE OR REPLACE FORCE VIEW  "V_CONCERTOS" ("ID_EVENTO", "NOMEFEST", "LOCAL", "DATA", "NUMBILHETES") AS 
  select ID_evento,
    nomefest,
local,
data,
numBilhetes from eventos e
where e.ID_evento not in (select ID_evento from v_festivais)
/

CREATE OR REPLACE FORCE VIEW  "V_EMPRESAS" ("ID_EMPRESA", "NOMEEMP", "SEDE", "TELEFONE") AS 
  select e.id_empresa, e.nomeemp, e.sede, e.telefone 
from empresas e
where e.id_empresa not in (select id_empresa from v_postos_venda)
/

CREATE OR REPLACE FORCE VIEW  "V_EVENTOS_EMPRESAS" ("PK", "ID_EVENTO", "ID_EMPRESA", "VALOR") AS 
  select ROWID pk, id_evento, id_empresa, valor
from eventos_empresas
/
​
CREATE OR REPLACE FORCE VIEW  "V_FESTIVAIS" ("ID_EVENTO", "NOMEFEST", "LOCAL", "DATA", "NUMDIAS", "NUMBILHETES") AS 
  select ID_evento, 
    nomeFest, 
    local,
    data,
numDias,
numBilhetes
from eventos natural inner join festivais
/

CREATE OR REPLACE FORCE VIEW  "V_VENDA_BILHETES" ("PK", "ID_EVENTO", "ID_EMPRESA", "PRECOBILH", "BVENDIDOS") AS 
  select ROWID pk, id_evento, id_empresa, precobilh, bvendidos
from venda_bilhetes
/

CREATE OR REPLACE FORCE VIEW  "V_POSTOS_VENDA" ("ID_EMPRESA", "NOMEEMP", "SEDE", "TELEFONE") AS 
  select id_empresa, nomeemp, sede, telefone
from postos_venda natural inner join empresas
/

CREATE OR REPLACE FORCE VIEW  "V_STAFF" ("NRBI", "NOMEP", "TELEFONE", "EMAIL", "ID_CARGO") AS 
  select nrBI, nomeP, telefone, email, id_cargo
from pessoas natural inner join staff
/
​CREATE OR REPLACE FORCE VIEW  "V_STAFF_EVENTOS" ("PK", "ID_STAFF", "ID_EVENTO") AS 
  select ROWID pk, id_staff, id_evento
from staff_eventos
/

create or replace TRIGGER banda_pk
  BEFORE INSERT ON bandas
  FOR EACH ROW
BEGIN
  SELECT seq_banda.nextval
    INTO :new.ID_banda
    FROM dual;
END;​
/
create or replace TRIGGER cargo_pk
  BEFORE INSERT ON cargo
  FOR EACH ROW
BEGIN
  SELECT seq_cargo.nextval
    INTO :new.ID_cargo
    FROM dual;
END;​
/
create or replace TRIGGER empresa_pk
  BEFORE INSERT ON empresas
  FOR EACH ROW
BEGIN
  SELECT seq_empresa.nextval
    INTO :new.ID_empresa
    FROM dual;
END;​
/
create or replace TRIGGER eventos_pk
  BEFORE INSERT ON eventos
  FOR EACH ROW
BEGIN
  SELECT seq_evento.nextval
    INTO :new.ID_evento
    FROM dual;
END;
/
create or replace trigger trg_insert_agentes
instead of insert on v_agentes
for each row
begin
insert into pessoas(nrBI, nomeP, telefone, email)
values(:NEW.nrBI, :NEW.nomeP, :NEW.telefone, :NEW.email);
insert into agentes(email)
values(:NEW.email);
end;​
/
create or replace trigger trg_insert_artistas
instead of insert on v_artistas_banda
for each row
begin
insert into pessoas(nrBI, nomeP, telefone, email)
values(:NEW.nrBI, :NEW.nomeP, :NEW.telefone, :NEW.email);
insert into artistas(email)
values(:NEW.email);
insert into bandas_artistas(ID_banda, ID_artista)
    values(:NEW.ID_banda, :NEW.email);
end;​
/
create or replace trigger trg_insert_bandas_eventos
instead of insert on v_bandas_eventos
for each row
begin
insert into bandas_eventos(cachet, id_evento, id_banda)
values(:NEW.cachet, :NEW.id_evento, :NEW.id_banda);
end;​
/
create or replace trigger trg_insert_concerto
instead of insert on v_concertos
for each row
begin
insert into eventos(id_evento, nomefest, local, data, numbilhetes)
values(:NEW.id_evento, :NEW.nomefest, :NEW.local, :NEW.data, :NEW.numbilhetes);
insert into concertos(id_evento)
values(:NEW.id_evento);
end;​
/
create or replace trigger trg_insert_eventos_empresas
instead of insert on v_eventos_empresas
for each row
begin
insert into eventos_empresas(id_evento, id_empresa, valor)
values(:NEW.id_evento, :NEW.id_empresa, :NEW.valor);
end;​
/
create or replace trigger trg_insert_festival
instead of insert on v_festivais
for each row
begin
insert into eventos(id_evento, nomefest, local, data, numbilhetes)
values(:NEW.id_evento, :NEW.nomefest, :NEW.local, :NEW.data, :NEW.numbilhetes);
insert into festivais(id_evento, numdias)
values(:NEW.id_evento, :NEW.numdias);
end;​
/
create or replace trigger trg_insert_staff
instead of insert on v_staff
for each row
begin
insert into pessoas(nrBI, nomeP, telefone, email)
values(:NEW.nrBI, :NEW.nomeP, :NEW.telefone, :NEW.email);
insert into staff(email, ID_cargo)
values(:NEW.email, :NEW.ID_cargo);
end;​
/
create or replace trigger trg_insert_staff_eventos
instead of insert on v_staff_eventos
for each row
begin
insert into staff_eventos(id_evento, id_staff)
values(:NEW.id_evento, :NEW.id_staff);
end;​
/
create or replace trigger trg_insert_venda_bilhetes
instead of insert on v_venda_bilhetes
for each row
begin
insert into venda_bilhetes(id_evento, id_empresa, precobilh, bvendidos)
values(:NEW.id_evento, :NEW.id_empresa, :NEW.precobilh, :NEW.bvendidos);
end;​
/
create or replace trigger trg_del_agentes
   instead of delete on v_agentes
   referencing new as new old as old
   begin
       delete from pessoas
       where email=:old.email;
       delete from agentes
       where email=:old.email;
   end;​
/
create or replace trigger trg_del_artistas
   instead of delete on v_artistas_banda
   referencing new as new old as old
   begin
       delete from pessoas
       where email=:old.email;
       delete from agentes
       where email=:old.email;
   end;​
/
create or replace TRIGGER TRG_Del_bandas_eventos
   INSTEAD OF DELETE ON v_bandas_eventos
   FOR EACH ROW
   BEGIN
     DELETE FROM bandas_eventos
     WHERE ROWID = :OLD.pk;
END;​
/
create or replace TRIGGER  "TRG_DEL_CONCERTO" 
   instead of delete on v_concertos
   referencing new as new old as old
   begin
       delete from eventos
       where id_evento=:old.id_evento;
       delete from concertos
       where id_evento=:old.id_evento;
   end;
/
create or replace TRIGGER TRG_del_eventos_empresas
INSTEAD OF DELETE ON v_eventos_empresas
FOR EACH ROW
BEGIN
DELETE FROM eventos_empresas
WHERE ROWID = :OLD.pk;
END;​
/
create or replace TRIGGER  "TRG_DEL_FESTIVAL" 
   instead of delete on v_festivais
   referencing new as new old as old
   begin
       delete from eventos
       where id_evento=:old.id_evento;
       delete from festivais
       where id_evento=:old.id_evento;
   end;
/
create or replace TRIGGER  "TRG_DEL_STAFF" 
   instead of delete on v_staff
   referencing new as new old as old
   begin
       delete from pessoas
       where email=:old.email;
       delete from staff
       where email=:old.email;
   end;
/
create or replace TRIGGER TRG_del_staff_eventos
INSTEAD OF DELETE ON v_staff_eventos
FOR EACH ROW
BEGIN
DELETE FROM staff_eventos
WHERE ROWID = :OLD.pk;
END;​
/
create or replace TRIGGER TRG_del_venda_bilhetes
INSTEAD OF DELETE ON v_venda_bilhetes
FOR EACH ROW
BEGIN
DELETE FROM venda_bilhetes
WHERE ROWID = :OLD.pk;
END;​
/
create or replace trigger trg_up_agentes
   instead of update on v_agentes
   referencing new as new old as old
   begin
       update pessoas
       set nrBI = :new.nrBI,
       nomeP = :new.nomeP,
       telefone = :new.telefone
       where email = :old.email;
   end;​
/
create or replace trigger trg_up_artistas
   instead of update on v_artistas_banda
   referencing new as new old as old
   begin
       update pessoas
       set nrBI = :new.nrBI,
       nomeP = :new.nomeP,
       telefone = :new.telefone,
       email = :new.email
       where email = :old.email;
   end;​
/
create or replace trigger trg_up_bandas_eventos
   instead of update on v_bandas_eventos
   referencing new as new old as old
   begin
       update bandas_eventos
       set id_evento = :new.id_evento,
       id_banda = :new.id_banda,
       cachet = :new.cachet
where ROWID = :NEW.pk;
   end;​
/
create or replace trigger trg_up_concertos
   instead of update on v_concertos
   referencing new as new old as old
   begin
       update eventos
       set nomefest = :new.nomefest,
       local = :new.local,
       data = :new.data,
       numbilhetes = :new.numbilhetes
       where id_evento = :old.id_evento;
   end;​
/
create or replace trigger trg_up_eventos_empresas
   instead of update on v_eventos_empresas
   referencing new as new old as old
   begin
       update eventos_empresas
       set id_evento = :new.id_evento,
       id_empresa = :new.id_empresa,
       valor = :new.valor
where ROWID = :NEW.pk;
   end;​
/
create or replace trigger trg_up_festivais
   instead of update on v_festivais
   referencing new as new old as old
   begin
       update eventos
       set nomefest = :new.nomefest,
       local = :new.local,
       data = :new.data,
       numbilhetes = :new.numbilhetes
       where id_evento = :old.id_evento;
       update festivais
           set numdias = :new.numdias
           where id_evento=:old.id_evento;
   end;​
/
create or replace trigger trg_up_staff
   instead of update on v_staff
   referencing new as new old as old
   begin
       update pessoas
       set nrBI = :new.nrBI,
       nomeP = :new.nomeP,
       telefone = :new.telefone
       where email = :old.email;
   end;​
/
create or replace trigger trg_up_staff_eventos
   instead of update on v_staff_eventos
   referencing new as new old as old
   begin
       update staff_eventos
       set id_evento = :new.id_evento,
       id_staff = :new.id_staff
where ROWID = :NEW.pk;
   end;​
/
create or replace trigger trg_up_staff_eventos
   instead of update on v_staff_eventos
   referencing new as new old as old
   begin
       update staff_eventos
       set id_evento = :new.id_evento,
       id_staff = :new.id_staff
where ROWID = :NEW.pk;
   end;​
/
create or replace FUNCTION bilhetesdisponiveis(ID number) return number
IS
    
var number;
bilhetes number; 
disponiveis number;

BEGIN
    select SUM(numbilhetes - bvendidos) into var from eventos e, venda_bilhetes v where e.ID_EVENTO = ID AND v.ID_EVENTO = ID;
    select numbilhetes into bilhetes from eventos e where e.ID_EVENTO = ID;

if var > bilhetes
   then disponiveis := var - bilhetes;
else
    disponiveis := var; 
END IF; 

return disponiveis;
 END bilhetesdisponiveis;
​/

create or replace FUNCTION receita(ID number) return number
IS
valor number;
BEGIN
    select NVL(TO_NUMBER(bvendidos), 0)*NVL(TO_NUMBER(precobilh), 0) + NVL(TO_NUMBER(valor), 0) - NVL(TO_NUMBER(cachet), 0) into valor 
from eventos e, venda_bilhetes v, eventos_empresas f, bandas_eventos b where e.ID_EVENTO = ID 
    AND v.ID_EVENTO = ID AND f.ID_Evento = ID AND b.ID_Evento = ID;
 return valor;
 END receita;
​
/

insert into pessoas values('15232342', 'Fernando Pessoa', '231700200', 'poesia-pessoa@poesias.pt');
insert into agentes values('poesia-pessoa@poesias.pt');

insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Metallica', 'Estados Unidos');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'MUSE', 'Reino Unido');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'AC/DC', 'Austrália');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'The Black Keys', 'Estados Unidos');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Buraka Som Sistema', 'Portugal');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Daft Punk', 'França');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Coldplay', 'Reino Unido');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Maroon 5', 'Estados Unidos');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Linkin Park', 'Estados Unidos');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Limp Bizkit', 'Estados Unidos');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Kaiser Chiefs', 'Reino Unido');
insert into bandas (ID_banda, nomeB, pais) values(seq_banda.nextval, 'Arcade Fire', 'Canadá');

insert into empresas values(seq_empresa.nextval, 'Fnac', 'Rua Carlos Alberto da Mota Pinto, Edifício.Amoreiras Plaza, 9, 6º.-B 1070-374 Lisboa', '707313435');
insert into empresas values(seq_empresa.nextval, 'Worten', 'Porto, Matosinhos, Senhora da Hora 4460-334 SENHORA DA HORA', '808100007');
insert into empresas values(seq_empresa.nextval, 'Ticketline', 'AV. Elias Garcia nº 137 - 3º andar 1050-099 Lisboa', '707234234');
insert into empresas values(seq_empresa.nextval, 'Bilheteira Online', 'Largo Eugénio Salvador 5-B 2790-340 Queijas Portugal', '214189031');
insert into empresas values(seq_empresa.nextval, 'Blueticket', 'Rossio dos Olivais Lote 2.13.01 A 1990-213 LISBOA', '707780000');
insert into empresas values(seq_empresa.nextval, 'PizzaHut', 'Avenida Infante D. Henrique - Lote 1 - 306 1900-408 Lisboa Portugal', '218390000');
insert into empresas values(seq_empresa.nextval, 'Telepizza', 'Rua General Firmino Miguel, Nr. 3, Torre 2 Piso 9 - A E B 1600-100 Lisboa PORTUGAL', '212740210');
insert into empresas values(seq_empresa.nextval, 'EDP', 'Praça Marquês de Pombal, 12 1250-162 Lisboa Portugal', '210012500');
insert into empresas values(seq_empresa.nextval, 'Vodafone', 'Av. D. João II, Lote 1.04.01, Parque das Nações, 1998 017 Lisboa', '16912');
insert into empresas values(seq_empresa.nextval, 'Optimus', 'Lugar do Espido Via Norte, 4471-909 MAIA', '931002117');
insert into empresas values(seq_empresa.nextval, 'PT', 'Av. Fontes Pereira de Melo, nº 40 1069-300 Lisboa', '16200');
insert into empresas values(seq_empresa.nextval, 'KFC', 'Edifício Península Praça do Bom Sucesso, 105 a 159 - 9º 4150-146 Porto Portugal', '226089700');

insert into postos_venda values(1);
insert into postos_venda values(2);
insert into postos_venda values(3);
insert into postos_venda values(4);
insert into postos_venda values(5);

insert into eventos values(seq_evento.nextval, 'Sudoeste TMN', 'Herderdade da Pomba',  to_date('10-JUL-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'Rock in Rio', 'Parque da Bela Vista',  to_date('30-MAY-2012', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'Pimbamisto', 'Quinta Rende o Peixe',  to_date('14-FEB-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'Super Bock Super Rock', 'Praia do Meco',  to_date('10-JUL-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'x no Pavilhão Atlântico', 'Pavilhão Atlântico',  to_date('12-MAY-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'y no Coliseu dos Recreios', 'Coliseu dos Recreios',  to_date('01-APR-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'Portugal Ao Vivo', 'Palácio de Cristal',  to_date('02-MAR-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'z no Coliseu do Porto', 'Coliseu do Porto',  to_date('03-FEB-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'k no Campo Pequeno', 'Campo Pequeno',  to_date('06-JAN-2013', 'DD-MON-YYYY'), 2000);
insert into eventos values(seq_evento.nextval, 'Optimus Alive', 'Passeio Maritimo de Algés',  to_date('07-MAR-2013', 'DD-MON-YYYY'), 2000);

insert into festivais values(1, 5);
insert into festivais values(2, 5);
insert into festivais values(3, 1);
insert into festivais values(4, 3);
insert into festivais values(10, 3);
insert into festivais values(7, 2);

insert into concertos values(5);
insert into concertos values(6);
insert into concertos values(8);
insert into concertos values(9);

insert into pessoas values('13856721', 'José Almiro', '931234567', 'ja@gmail.com');
insert into pessoas values('14531231', 'Almeida Garret', '252323560', 'ag@hotmail.com');
insert into pessoas values('10283873', 'Cavaco Silva', '707121212', 'cavaco@psd.pt');
insert into pessoas values('19891223', 'Pedro Passos Coelho', '112112112', 'ppc@tudo.pt');
insert into pessoas values('10934081', 'José Socrates', '934000764', 'zesocas@filosofo.fr');
insert into pessoas values('18989732', 'Luis Filipe Orelhas', '914334335', 'orelhas@slb.pt');
insert into pessoas values('03453439', 'James Hetfield', '345678934', 'james@metallica.com');
insert into pessoas values('03453239', 'Lars Ulrich', '345678934', 'lars@metallica.com');
insert into pessoas values('03453249', 'Rober Trujillo', '345678934', 'robert@metallica.com');
insert into pessoas values('03453259', 'Kirk Hammet', '345678934', 'kirk@metallica.com');
insert into pessoas values('03453436', 'Matt Bellamy', '343123456', 'mb@muse.mu'); 
insert into pessoas values('03453435', 'Brian Johnson', '345675235', 'bj@acdc.com'); 
insert into pessoas values('11111129', 'Patrick Carney', '213343212', 'pc@blackkeys.com'); 
insert into pessoas values('11111139', 'Rafael Timolho', '123123345', 'timolho@buraka.pt'); 
insert into pessoas values('11111149', 'Thomas Bangalter', '123123123', 'thomas@dp.fr');
insert into pessoas values('222333409', 'Chris Martin', '700800910', 'cm@coldplay.uk'); 
insert into pessoas values('222333410', 'Adam Levine', '700800911', 'adam@five.com'); 
insert into pessoas values('222333411', 'Chester Bennington', '700800912', 'chester@lp.com');
insert into pessoas values('222333412', 'Fred Dust', '700800913', 'fd@bizkit.com'); 
insert into pessoas values('222333413', 'Ricky Wilson', '700800914', 'rw@chiefs.uk'); 
insert into pessoas values('222333414', 'Win Butler ', '700800916', 'butler@arcadefire.cd'); 
insert into pessoas values('10348972', 'Gonçalo Delgado', '913453460', 'gonçalo@ritmos.pt');
insert into pessoas values('10834294', 'Daniel Adelino', '913453461', 'daniel@ritmos.pt');
insert into pessoas values('12129337', 'Guilherme Fião', '913453462', 'g.fiao@ritmos.pt');

insert into artistas values('james@metallica.com');
insert into artistas values('lars@metallica.com');
insert into artistas values('robert@metallica.com');
insert into artistas values('kirk@metallica.com');
insert into artistas values('mb@muse.mu');
insert into artistas values('bj@acdc.com');
insert into artistas values('pc@blackkeys.com');
insert into artistas values('timolho@buraka.pt');
insert into artistas values('thomas@dp.fr');
insert into artistas values('cm@coldplay.uk');
insert into artistas values('adam@five.com');
insert into artistas values('chester@lp.com');
insert into artistas values('fd@bizkit.com');
insert into artistas values('rw@chiefs.uk');
insert into artistas values('butler@arcadefire.cd');

insert into cargo values(seq_cargo.nextval, 'CEO');
insert into cargo values(seq_cargo.nextval, 'Gestor');
insert into cargo values(seq_cargo.nextval, 'Voluntário');
insert into cargo values(seq_cargo.nextval, 'RP');

insert into staff values('gonçalo@ritmos.pt', 2);
insert into staff values('daniel@ritmos.pt', 3);
insert into staff values('g.fiao@ritmos.pt', 4);

insert into agentes values('ja@gmail.com');
insert into agentes values('ag@hotmail.com');

insert into eventos_empresas values(5000, 1, 6);
insert into eventos_empresas values(5000, 1, 8);
insert into eventos_empresas values(5000, 2, 9);
insert into eventos_empresas values(5000, 2, 10);

insert into venda_bilhetes values(30, 0, 1, 1);
insert into venda_bilhetes values(29, 0, 2, 2);

insert into bandas_eventos values(1000, 2, 1);
insert into bandas_eventos values(1200, 1, 2);

insert into bandas_artistas values(1, 'james@metallica.com');
insert into bandas_artistas values(1, 'lars@metallica.com');
insert into bandas_artistas values(1, 'robert@metallica.com');
insert into bandas_artistas values(2, 'mb@muse.mu');
insert into bandas_artistas values(3, 'bj@acdc.com');
insert into bandas_artistas values(4, 'pc@blackkeys.com');
insert into bandas_artistas values(5, 'timolho@buraka.pt');
insert into bandas_artistas values(6, 'thomas@dp.fr');
insert into bandas_artistas values(7, 'cm@coldplay.uk');
insert into bandas_artistas values(8, 'adam@five.com');
insert into bandas_artistas values(9, 'chester@lp.com');
insert into bandas_artistas values(10, 'fd@bizkit.com');
insert into bandas_artistas values(11, 'rw@chiefs.uk');
insert into bandas_artistas values(12, 'butler@arcadefire.cd');


insert into staff_eventos values(1, 'gonçalo@ritmos.pt');
insert into staff_eventos values(1, 'g.fiao@ritmos.pt');


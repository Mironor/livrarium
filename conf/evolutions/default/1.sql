# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "books" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"idUser" BIGINT NOT NULL,"uuid" UUID NOT NULL,"name" VARCHAR(254) NOT NULL,"format" VARCHAR(254) NOT NULL,"totalPages" INTEGER NOT NULL,"currentPage" INTEGER NOT NULL);
create unique index "UNIQUE_UUID" on "books" ("uuid");
create table "books_to_folders" ("idBook" BIGINT NOT NULL,"idFolder" BIGINT NOT NULL);
alter table "books_to_folders" add constraint "pk_books_to_folders" primary key("idBook","idFolder");
create table "folders" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"idUser" BIGINT NOT NULL,"name" VARCHAR(254) NOT NULL,"level" INTEGER NOT NULL,"left" INTEGER NOT NULL,"right" INTEGER NOT NULL);
create table "logininfos" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"idUser" BIGINT NOT NULL,"providerID" VARCHAR(254) NOT NULL,"providerKey" VARCHAR(254) NOT NULL);
create table "oauth1infos" ("idLoginInfo" BIGINT NOT NULL,"token" VARCHAR(254) NOT NULL,"secret" VARCHAR(254) NOT NULL);
create table "oauth2infos" ("idLoginInfo" BIGINT NOT NULL,"accesstoken" VARCHAR(254) NOT NULL,"tokentype" VARCHAR(254),"expiresin" INTEGER,"refreshtoken" VARCHAR(254));
create table "passwordinfos" ("idLoginInfo" BIGINT NOT NULL,"hasher" VARCHAR(254) NOT NULL,"password" VARCHAR(254) NOT NULL,"salt" VARCHAR(254));
create table "users" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"email" VARCHAR(254) NOT NULL,"avatarURL" VARCHAR(254));
alter table "books" add constraint "BOOK_USER_FK" foreign key("idUser") references "users"("id") on update RESTRICT on delete CASCADE;
alter table "books_to_folders" add constraint "BOOK_BOOKS_TO_FOLDERS_FK" foreign key("idBook") references "books"("id") on update RESTRICT on delete CASCADE;
alter table "books_to_folders" add constraint "FOLDER_BOOKS_TO_FOLDERS_FK" foreign key("idFolder") references "folders"("id") on update RESTRICT on delete CASCADE;
alter table "folders" add constraint "FOLDER_USER_FK" foreign key("idUser") references "users"("id") on update RESTRICT on delete CASCADE;
alter table "logininfos" add constraint "LOGININFO_USER_FK" foreign key("idUser") references "users"("id") on update RESTRICT on delete CASCADE;
alter table "oauth1infos" add constraint "OAUTH1INFO_LOGININFO_FK" foreign key("idLoginInfo") references "logininfos"("id") on update RESTRICT on delete CASCADE;
alter table "oauth2infos" add constraint "OAUTH2INFO_LOGININFO_FK" foreign key("idLoginInfo") references "logininfos"("id") on update RESTRICT on delete CASCADE;
alter table "passwordinfos" add constraint "PASSWORDINFO_LOGININFO_FK" foreign key("idLoginInfo") references "logininfos"("id") on update RESTRICT on delete CASCADE;

# --- !Downs

alter table "passwordinfos" drop constraint "PASSWORDINFO_LOGININFO_FK";
alter table "oauth2infos" drop constraint "OAUTH2INFO_LOGININFO_FK";
alter table "oauth1infos" drop constraint "OAUTH1INFO_LOGININFO_FK";
alter table "logininfos" drop constraint "LOGININFO_USER_FK";
alter table "folders" drop constraint "FOLDER_USER_FK";
alter table "books_to_folders" drop constraint "BOOK_BOOKS_TO_FOLDERS_FK";
alter table "books_to_folders" drop constraint "FOLDER_BOOKS_TO_FOLDERS_FK";
alter table "books" drop constraint "BOOK_USER_FK";
drop table "users";
drop table "passwordinfos";
drop table "oauth2infos";
drop table "oauth1infos";
drop table "logininfos";
drop table "folders";
alter table "books_to_folders" drop constraint "pk_books_to_folders";
drop table "books_to_folders";
drop table "books";


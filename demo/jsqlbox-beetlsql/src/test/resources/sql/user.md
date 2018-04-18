select
===
select * from users where 1=1
@if(!isEmpty(age)){
and age > #age#
@}
@if(!isEmpty(name)){
and name = #name#
@}

selectUserEntity
===
select u.** from users u where 1=1
@if(!isEmpty(u.age)){
and u.age > #u.age#
@}
@if(!isEmpty(u.name)){
and u.name = #u.name#
@}
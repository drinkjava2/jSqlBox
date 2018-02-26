select
===
select * from users where 1=1
@if(!isEmpty(age)){
and age > #age#
@}
@if(!isEmpty(name)){
and name = #name#
@}
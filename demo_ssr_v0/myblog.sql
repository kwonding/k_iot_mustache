drop database if exists myblog; 
create database myblog;

use myblog;

select * from board_tb;
select * from user_tb;
select * from user_role_tb;
select * from purchase_tb;
select * from payment_tb;
select * from refund_request_tb;

select u.*, r.role
from user_tb u
left join user_role_tb r
	on u.id = r.user_id
where u.username = 'aaa';

insert into user_role_tb(user_id, role) values(8, 'USER');
insert into user_role_tb(user_id, role) values(8, 'ADMIN');
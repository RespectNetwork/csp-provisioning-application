insert into csp
(	csp_cloudname
,	payment_gateway_name
,	payment_url_template
,	username
,	password
,	cost_per_cloudname
,	currency
,	time_created
)
values 
(	'@testcsp'
,	'payment gateway'
,	'https://www.pay.com/{xid}/{currency}/{amount}'
,	'username'
,	'password'
,	25.00
,	'USD'
,	now()
);
insert into csp
(	csp_cloudname
,	payment_gateway_name
,	payment_url_template
,	username
,	password
,	cost_per_cloudname
,	currency
,	time_created
)
values 
(	'@testcsp1'
,	'payment gateway 1'
,	'https://www.pay.com/{xid}/{currency}/{amount}'
,	'username1'
,	'password1'
,	25.00
,	'USD'
,	now()
);

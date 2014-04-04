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
(	'+testcsp'
,	'STRIPE'
,	'https://checkout.stripe.com/checkout.js'
,	'pk_test_BWUFT9rT63JEL5829geBwdVS'
,	'sk_test_edYuRLMvloToRshYM49kO1Gz'
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
(	'+paoga'
,	'SAGEPAY'
,	'https://checkout.stripe.com/checkout.js'
,	'pk_test_BWUFT9rT63JEL5829geBwdVS'
,	'sk_test_edYuRLMvloToRshYM49kO1Gz'
,	25.00
,	'USD'
,	now()
);

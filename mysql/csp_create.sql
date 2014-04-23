create table  csp
(	csp_cloudname		varchar(255)	primary key
,	payment_gateway_name	varchar(64)	not null
,	payment_url_template	varchar(64)	not null
,	username		varchar(64)	not null
,	password		varchar(64)	not null
,	cost_per_cloudname	numeric(9,2)	not null
,	currency		char(3)		not null
,	time_created		datetime	not null
, 	user_key				varchar(128)
, 	enc_key				varchar(2048)
, 	env					varchar(64)
);

create table  payment
(	payment_id		char(36)	primary key
,	csp_cloudname		varchar(255)	not null
,	payment_reference_id	varchar(64)	not null
,	payment_response_code	varchar(64)	not null
,	amount			numeric(9,2)	not null
,	currency		char(3)		not null
,	time_created		datetime	not null
);
create index ix_payment_csp_cloudname on payment(csp_cloudname);
alter  table payment add (constraint fk_payment_csp_cloudname foreign key (csp_cloudname) references csp(csp_cloudname));

create table  csp_cost_override
(	csp_cloudname		varchar(255)	not null
,	phone_prefix	varchar(64)	not null
,	cost_per_cloudname	numeric(9,2)	not null
,	currency		char(3)		not null
,   merchant_account_id varchar(255) not null
,   PRIMARY KEY (csp_cloudname, phone_prefix)
);
create index ix_csp_cost_override on csp_cost_override(csp_cloudname);
alter  table csp_cost_override add (constraint fk_csp_cost_override_csp_cloudname foreign key (csp_cloudname) references csp(csp_cloudname));

create table  invite
(	invite_id		char(36)	primary key
,	csp_cloudname		varchar(255)	not null
,	inviter_cloudname	varchar(255)	not null
,	invited_email_address	varchar(128)	not null
,	email_subject		varchar(256)	not null
,	email_message		varchar(2048)	not null
,	time_created		datetime	not null
);

create index ix_invite_inviter_cloudname     on invite(inviter_cloudname);
create index ix_invite_csp_cloudname         on invite(csp_cloudname);
alter  table invite add (constraint fk_invite_csp_cloudname foreign key (csp_cloudname) references csp(csp_cloudname));

create table  invite_response
(	response_id		char(36)	primary key
,	invite_id		char(36)	not null
,	payment_id		char(36)	not null
,	cloudname_created	varchar(255)	not null
,	time_created		datetime	not null
);

create        index ix_invite_response_invite_id           on invite_response(invite_id);
create        index ix_invite_response_payment_id          on invite_response(payment_id);
create unique index ix_invite_response_cloudname_created   on invite_response(cloudname_created);
alter  table invite_response add (constraint fk_invite_response_invite_id  foreign key (invite_id)  references invite(invite_id));
alter  table invite_response add (constraint fk_invite_response_payment_id foreign key (payment_id) references payment(payment_id));

create table  giftcode
(	giftcode_id		char(36)	primary key
,	invite_id		char(36)	not null
,	payment_id		char(36)	not null
,	time_created		datetime	not null
);

create index ix_giftcode_invite_id  on giftcode(invite_id);
create index ix_giftcode_payment_id on giftcode(payment_id);
alter  table giftcode add (constraint fk_giftcode_invite_id  foreign key (invite_id)  references invite(invite_id));
alter  table giftcode add (constraint fk_giftcode_payment_id foreign key (payment_id) references payment(payment_id));

create table  giftcode_redemption
(	redemption_id		char(36)	primary key
,	giftcode_id		char(36)	not null
,	cloudname_created	varchar(255)	not null
,	time_created		datetime	not null
);

create unique index ix_giftcode_redemption_giftcode_id         on giftcode_redemption(giftcode_id);
create unique index ix_giftcode_redemption_cloudname_created   on giftcode_redemption(cloudname_created);
alter  table giftcode_redemption add (constraint fk_giftcode_redemption_giftcode_id foreign key (giftcode_id) references giftcode(giftcode_id));

create table  dependent_cloud
(	guardian_cloudname	varchar(255)	not null
,	dependent_cloudname	varchar(255)	not null
,	payment_id		varchar(36)	not null
,	time_created		datetime	not null
,	primary key (guardian_cloudname, dependent_cloudname)
);

create table  signup_info
(	cloudname	varchar(255)	not null
,	email	varchar(128)	
,	phone		varchar(36)	
,	primary key (cloudname)
);

create table promo_code
( 	promo_id 		varchar(64),
	start_date 		datetime,
	end_date		datetime,
	promo_limit 	int(11)
);

create table promo_cloud
(	promo_id 		varchar(64),
	cloudname		varchar(255),
	creation_date	datetime,
	csp_cloudname	varchar(255)
);




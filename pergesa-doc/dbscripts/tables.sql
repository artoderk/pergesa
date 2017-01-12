-- Postgres
CREATE TABLE event_storage
(
  id bigint,
  tag smallint,
  system_id character varying(64),
  business_id character varying(64),
  business_type character varying(64),
  status smallint,
  payload text,
  retried_count_d smallint,
  retried_count_c smallint,
  next_retry_time timestamp without time zone,
  memo text,
  gmt_created timestamp without time zone,
  gmt_modified timestamp without time zone,
  CONSTRAINT tran_info_pkey PRIMARY KEY (id, tag, system_id)
);

create index idx_event_storage_ts on event_storage(tx_status);
create index idx_event_storage_gm on event_storage(gmt_modified);

commit;
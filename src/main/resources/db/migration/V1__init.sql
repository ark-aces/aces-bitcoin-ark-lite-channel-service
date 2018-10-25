CREATE TABLE contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(20),
  recipient_ark_address VARCHAR(255),
  return_btc_address VARCHAR(255),
  deposit_btc_address VARCHAR(255),
  deposit_btc_address_private_key VARCHAR(255),
  created_at TIMESTAMP
);
CREATE INDEX ON contracts (id);
CREATE INDEX ON contracts (correlation_id);
CREATE INDEX ON contracts (status);
CREATE INDEX ON contracts (recipient_ark_address);
CREATE INDEX ON contracts (deposit_btc_address);
CREATE INDEX ON contracts (return_btc_address);
CREATE INDEX ON contracts (created_at);


CREATE TABLE transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  btc_transaction_id VARCHAR(255),
  btc_amount DECIMAL(40,8),
  btc_to_ark_rate DECIMAL(40,8),
  btc_flat_fee DECIMAL(40,8),
  btc_percent_fee DECIMAL(40,8),
  btc_total_fee DECIMAL(40,8),
  ark_send_amount DECIMAL(40,8),
  ark_transaction_id VARCHAR(255),
  needs_ark_confirmation BOOLEAN,
  ark_confirmation_subscription_id VARCHAR(255),
  needs_btc_return BOOLEAN,
  return_btc_transaction_id VARCHAR(255)
);
ALTER TABLE transfers ADD FOREIGN KEY (contract_pid) REFERENCES contracts (pid);

CREATE INDEX ON transfers (id);
CREATE INDEX ON transfers (created_at);
CREATE INDEX ON transfers (contract_pid);
CREATE INDEX ON transfers (status);
CREATE INDEX ON transfers (btc_transaction_id);
CREATE INDEX ON transfers (btc_amount);
CREATE INDEX ON transfers (ark_transaction_id);
CREATE INDEX ON transfers (return_btc_transaction_id);


CREATE TABLE service_capacities (
  pid BIGSERIAL PRIMARY KEY,
  available_amount DECIMAL(40, 8),
  unsettled_amount DECIMAL(40, 8),
  total_amount DECIMAL(40, 8),
  unit VARCHAR(20),
  updated_at TIMESTAMP,
  created_at TIMESTAMP
);
CREATE INDEX ON service_capacities (available_amount);
CREATE INDEX ON service_capacities (unsettled_amount);
CREATE INDEX ON service_capacities (total_amount);
CREATE INDEX ON service_capacities (unit);
CREATE INDEX ON service_capacities (updated_at);
CREATE INDEX ON service_capacities (created_at);

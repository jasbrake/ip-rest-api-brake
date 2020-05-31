package com.trillion.ipserver.address;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String>, CustomAddressSave {
}

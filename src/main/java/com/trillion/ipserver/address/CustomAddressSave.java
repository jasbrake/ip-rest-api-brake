package com.trillion.ipserver.address;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

interface CustomAddressSave {
  List<Address> saveAllNew(Iterable<Address> addresses);
}

class CustomAddressSaveImpl implements CustomAddressSave {
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * saveAllNew will only persist new Addresses and never merge with existing ones like save.
   *
   * @param addresses the addresses to save
   */
  @Transactional
  @Override
  public List<Address> saveAllNew(Iterable<Address> addresses) {
    List<Address> newAddresses = new ArrayList<>();
    for (Address a : addresses) {
      Address ref = entityManager.find(Address.class, a.getIp());
      if (ref == null) {
        entityManager.persist(a);
        ref = a;
      }
      newAddresses.add(ref);
    }
    return newAddresses;
  }
}

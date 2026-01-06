package org.httt2.hrms.bonus.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.httt2.hrms.bonus.dto.HistoryType;
import org.httt2.hrms.bonus.dto.SortDirection;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.entity.TransferType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransferTransactionRepositoryImpl
        implements TransferTransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<TransferTransaction> findFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types,
            String sortField,
            SortDirection direction) {
        // delegate to limited variant with no limit
        return findFilteredForEmployee(empId, from, to, types, sortField, direction, null);
    }

    @Override
    public List<TransferTransaction> findFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types,
            String sortField,
            SortDirection direction,
            Integer limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TransferTransaction> cq = cb.createQuery(TransferTransaction.class);
        Root<TransferTransaction> root = cq.from(TransferTransaction.class);
        Join<Object, Object> sender = root.join("sender");
        Join<Object, Object> receiver = root.join("receiver");

        List<Predicate> predicates = new ArrayList<>();

        // basic: transfers involving empId
        predicates.add(cb.or(
                cb.equal(sender.get("empId"), empId),
                cb.equal(receiver.get("empId"), empId)));

        // date range
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(to, LocalTime.MAX)));
        }

        // types filter (translate HistoryType into DB predicates)
        if (types != null && !types.isEmpty()) {
            List<Predicate> typePreds = new ArrayList<>();
            for (HistoryType ht : types) {
                switch (ht) {
                    case MONTHLY:
                        typePreds.add(cb.equal(root.get("type"), TransferType.MONTHLY));
                        break;
                    case AWARD:
                        typePreds.add(cb.equal(root.get("type"), TransferType.AWARD));
                        break;
                    case DEDUCT:
                        typePreds.add(cb.equal(root.get("type"), TransferType.DEDUCT));
                        break;
                    case TRANSFER_SENT:
                        typePreds.add(cb.and(
                                cb.equal(sender.get("empId"), empId),
                                cb.or(cb.isNull(root.get("type")), cb.equal(root.get("type"), TransferType.TRANSFER))));
                        break;
                    case TRANSFER_RECEIVED:
                        typePreds.add(cb.and(
                                cb.equal(receiver.get("empId"), empId),
                                cb.or(cb.isNull(root.get("type")), cb.equal(root.get("type"), TransferType.TRANSFER))));
                        break;
                    default:
                        break;
                }
            }
            predicates.add(cb.or(typePreds.toArray(new Predicate[0])));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // sort
        if (sortField == null || sortField.isEmpty() || "createdAt".equals(sortField)) {
            if (direction == null || direction == SortDirection.DESC) {
                cq.orderBy(cb.desc(root.get("createdAt")));
            } else {
                cq.orderBy(cb.asc(root.get("createdAt")));
            }
        } else {
            // fallback to createdAt if unknown field
            if (direction == null || direction == SortDirection.DESC) {
                cq.orderBy(cb.desc(root.get("createdAt")));
            } else {
                cq.orderBy(cb.asc(root.get("createdAt")));
            }
        }

        var query = em.createQuery(cq);
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    @Override
    public long countFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TransferTransaction> root = cq.from(TransferTransaction.class);
        Join<Object, Object> sender = root.join("sender");
        Join<Object, Object> receiver = root.join("receiver");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.or(
                cb.equal(sender.get("empId"), empId),
                cb.equal(receiver.get("empId"), empId)));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(to, LocalTime.MAX)));
        }

        if (types != null && !types.isEmpty()) {
            List<Predicate> typePreds = new ArrayList<>();
            for (HistoryType ht : types) {
                switch (ht) {
                    case MONTHLY:
                        typePreds.add(cb.equal(root.get("type"), TransferType.MONTHLY));
                        break;
                    case AWARD:
                        typePreds.add(cb.equal(root.get("type"), TransferType.AWARD));
                        break;
                    case DEDUCT:
                        typePreds.add(cb.equal(root.get("type"), TransferType.DEDUCT));
                        break;
                    case TRANSFER_SENT:
                        typePreds.add(cb.and(
                                cb.equal(sender.get("empId"), empId),
                                cb.or(cb.isNull(root.get("type")), cb.equal(root.get("type"), TransferType.TRANSFER))));
                        break;
                    case TRANSFER_RECEIVED:
                        typePreds.add(cb.and(
                                cb.equal(receiver.get("empId"), empId),
                                cb.or(cb.isNull(root.get("type")), cb.equal(root.get("type"), TransferType.TRANSFER))));
                        break;
                    default:
                        break;
                }
            }
            predicates.add(cb.or(typePreds.toArray(new Predicate[0])));
        }

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }
}
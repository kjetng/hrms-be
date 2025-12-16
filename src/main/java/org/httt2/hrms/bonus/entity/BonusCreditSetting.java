package org.httt2.hrms.bonus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bonus_credit_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusCreditSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer baseBonusCredits;

    @Column(nullable = false)
    private Double conversionRate;

    @Column(nullable = false)
    private LocalDate creditDate;
}

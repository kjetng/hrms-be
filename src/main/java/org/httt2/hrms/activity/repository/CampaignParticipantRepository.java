package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.entity.CampaignParticipant;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignParticipantRepository extends JpaRepository<CampaignParticipant, CampaignParticipantId> {
    
    // Kiểm tra xem nhân viên (theo empId) đã tham gia chiến dịch này chưa
    // Spring Data JPA sẽ tự parse: Id -> EmpId và Id -> CampaignId
    boolean existsByIdEmpIdAndIdCampaignId(Long empId, Long campaignId);
    
    // Đếm số lượng người tham gia
    long countByIdCampaignId(Long campaignId);

    List<CampaignParticipant> findByEmployee_EmpId(Long empId);
}
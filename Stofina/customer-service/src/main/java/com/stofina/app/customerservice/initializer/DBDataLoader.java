package com.stofina.app.customerservice.initializer;

import com.stofina.app.customerservice.enums.CustomerStatus;
import com.stofina.app.customerservice.model.CorporateCustomer;
import com.stofina.app.customerservice.model.Customer;
import com.stofina.app.customerservice.model.IndividualCustomer;
import com.stofina.app.customerservice.repository.CorporateCustomerRepository;
import com.stofina.app.customerservice.repository.CustomerRepository;
import com.stofina.app.customerservice.repository.IndividualCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DBDataLoader {

    private final CustomerRepository customerRepo;
    private final IndividualCustomerRepository individualRepo;
    private final CorporateCustomerRepository corporateRepo;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        log.info("Seeding mock customers...");
        seedIndividuals();
        seedCorporates();
        log.info("Mock customers done.");
    }

    private void seedIndividuals() {
        createIndividual(
                "11111111111", "Ada", "Yılmaz", "5551112233", "ada@example.com",
                "Ankara / Çankaya, Karanfil Sk. 10", CustomerStatus.ACTIVE
        );
        createIndividual(
                "22222222222", "Mert", "Kaya", "5552223344", "mert.kaya@example.com",
                "İstanbul / Kadıköy, Bahariye Cd. 25", CustomerStatus.ACTIVE
        );
        createIndividual(
                "33333333333", "Derya", "Şen", "5553334455", "derya.sen@example.com",
                "İzmir / Karşıyaka, Girne Blv. 5", CustomerStatus.ACTIVE
        );
        createIndividual(
                "44444444444", "Baran", "Acar", "5554445566", "baran.acar@example.com",
                "Bursa / Nilüfer, FSM Blv. 120", CustomerStatus.ACTIVE
        );
        createIndividual(
                "55555555555", "Elif", "Demir", "5555556677", "elif.demir@example.com",
                "Antalya / Muratpaşa, Atatürk Cd. 8", CustomerStatus.ACTIVE
        );
    }

    private void seedCorporates() {
        createCorporate(
                "Troya Yazılım A.Ş.", "TR-2024-0001", "9876543210", "Beşiktaş V.D.",
                "Cihan Dilsiz", "66666666666", "5320000011", "cihan.dilsiz@troya.com",
                "İstanbul / Beşiktaş, Levent Mah. 1", CustomerStatus.ACTIVE
        );
        createCorporate(
                "Ege Teknoloji Ltd.", "EG-2023-0101", "1122334455", "Konak V.D.",
                "Efe Altop", "77777777777", "5320000022", "efe.altop@ege.com",
                "İzmir / Konak, Alsancak 2", CustomerStatus.ACTIVE
        );
        createCorporate(
                "Marmara Finans A.Ş.", "MR-2022-0303", "5566778899", "Şişli V.D.",
                "Seda Arı", "88888888888", "5320000033", "seda.ari@marmara.com",
                "İstanbul / Şişli, Mecidiyeköy 3", CustomerStatus.ACTIVE
        );
        createCorporate(
                "Anadolu Lojistik A.Ş.", "AN-2021-0404", "6677889900", "Yenimahalle V.D.",
                "Kaan Öztürk", "99999999999", "5320000044", "kaan.ozturk@anadolu.com",
                "Ankara / Yenimahalle, Ostim 4", CustomerStatus.ACTIVE
        );
        createCorporate(
                "Akdeniz Enerji Ltd.", "AK-2020-0505", "3344556677", "Kepez V.D.",
                "Leyla Yalçın", "12312312312", "5320000055", "leyla.yalcin@akdeniz.com",
                "Antalya / Kepez, Düden 5", CustomerStatus.ACTIVE
        );
    }

    private void createIndividual(
            String tckn, String firstName, String lastName, String phone, String email,
            String legalAddress, CustomerStatus status
    ) {
        if (individualRepo.existsByTckn(tckn) || individualRepo.existsByEmail(email)) {
            log.info("Individual exists (tckn/email): {}/{}", tckn, email);
            return;
        }

        Customer base = customerRepo.saveAndFlush(
                Customer.builder()
                        .legalAddress(legalAddress)
                        .status(status)
                        .build()
        );

        IndividualCustomer ind = IndividualCustomer.builder()
                .customer(base)
                .tckn(tckn)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .email(email)
                .build();

        individualRepo.save(ind);
        log.info("Individual created: {} {} (id={})", firstName, lastName, ind.getId());
    }

    private void createCorporate(
            String tradeName, String tradeRegistryNumber, String taxNumber, String taxOffice,
            String repName, String repTckn, String repPhone, String repEmail,
            String legalAddress, CustomerStatus status
    ) {
        if (corporateRepo.existsByTradeRegistryNumber(tradeRegistryNumber)
                || corporateRepo.existsByTaxNumber(taxNumber)
                || corporateRepo.existsByRepresentativeTckn(repTckn)
                || corporateRepo.existsByRepresentativeEmail(repEmail)) {
            log.info("Corporate exists (registry/tax/rep): {}/{}/{}", tradeRegistryNumber, taxNumber, repTckn);
            return;
        }

        Customer base = customerRepo.saveAndFlush(
                Customer.builder()
                        .legalAddress(legalAddress)
                        .status(status)
                        .build()
        );

        CorporateCustomer corp = CorporateCustomer.builder()
                .customer(base)
                .tradeName(tradeName)
                .tradeRegistryNumber(tradeRegistryNumber)
                .taxNumber(taxNumber)
                .taxOffice(taxOffice)
                .representativeName(repName)
                .representativeTckn(repTckn)
                .representativePhone(repPhone)
                .representativeEmail(repEmail)
                .build();

        corporateRepo.save(corp);
        log.info("Corporate created: {} (id={})", tradeName, corp.getId());
    }
}

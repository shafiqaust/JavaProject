package io.silverstring.web.controller.view.dashboard;

import io.silverstring.core.repository.hibernate.CoinRepository;
import io.silverstring.core.repository.hibernate.UserRepository;
import io.silverstring.core.service.*;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.OtpStatus;
import io.silverstring.domain.hibernate.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {
    private final NoticeService noticeService;
    private final SupportService supportService;
    private final NewsService newsService;
    private final WalletService walletService;
    private final CoinService coinService;
    private final LevelService levelService;
    private final OtpService otpService;
    private final IcoRecommendService icoRecommendService;
    private final CoinRepository coinRepository;
    private final UserRepository userRepository;

    @Autowired
    public HomeController(NoticeService noticeService, SupportService supportService, NewsService newsService, WalletService walletService, CoinService coinService, LevelService levelService, OtpService otpService, IcoRecommendService icoRecommendService, CoinRepository coinRepository, UserRepository userRepository) {
        this.noticeService = noticeService;
        this.supportService = supportService;
        this.newsService = newsService;
        this.walletService = walletService;
        this.coinService = coinService;
        this.levelService = levelService;
        this.otpService = otpService;
        this.icoRecommendService = icoRecommendService;
        this.coinRepository = coinRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping("/deposit_manage")
    public ModelAndView depositManage(@ModelAttribute User user, @RequestParam(value = "selectionCoin", defaultValue = "BITCOIN") String selectionCoin, @RequestParam(value = "testMaster", defaultValue = "N") String testMaster) {
        if(((selectionCoin != null) && !selectionCoin.equals(""))) {
            if (!validCoinEnum(selectionCoin)) {
                return null;
            }
        }

        Coin coin = coinService.getCoin(CoinEnum.valueOf(selectionCoin));
        if (testMaster.equals("N") && !coin.getActive().equals(ActiveEnum.Y)) {
            return null;
        }

        ModelAndView mvn = new ModelAndView("dashboard/deposit_manage");
        mvn.addObject("selectionCoin", selectionCoin);
        WalletDTO.WalletInfos walletInfos =  walletService.getMyWallets(user.getId(), user.getLevel(), testMaster.equals("Y"));
        mvn.addObject("walletInfos", walletInfos);


        return mvn;
    }

    @RequestMapping("/test")
    public ModelAndView test(@ModelAttribute User user, @RequestParam(value = "selectionCoin", defaultValue = "ETHEREUM") String selectionCoin, @RequestParam(value = "testMaster", defaultValue = "N") String testMaster) {
        if(((selectionCoin != null) && !selectionCoin.equals(""))) {
            if (!validCoinEnum(selectionCoin)) {
                return null;
            }
        }

        Coin coin = coinService.getCoin(CoinEnum.valueOf(selectionCoin));
        if (testMaster.equals("N") && !coin.getActive().equals(ActiveEnum.Y)) {
            return null;
        }

        ModelAndView mvn = new ModelAndView("dashboard/sasha");
        mvn.addObject("selectionCoin", selectionCoin);

        if(testMaster.equals("N")) {
            mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), true));
        } else {
            mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), false));
        }

        return mvn;
    }

    @RequestMapping("/withdrawal_manage")
    public ModelAndView withdrawalManage(@ModelAttribute User user, @RequestParam(value = "selectionCoin", defaultValue = "BITCOIN") String selectionCoin, @RequestParam(value = "testMaster", defaultValue = "N") String testMaster) {
        if(((selectionCoin != null) && !selectionCoin.equals(""))) {
            if (!validCoinEnum(selectionCoin)) {
                return null;
            }
        }

        Coin coin = coinService.getCoin(CoinEnum.valueOf(selectionCoin));
        if (testMaster.equals("N") && !coin.getActive().equals(ActiveEnum.Y)) {
            return null;
        }

        ModelAndView mvn = new ModelAndView("dashboard/withdrawal_manage");
        mvn.addObject("selectionCoin", selectionCoin);

        if(testMaster.equals("N")) {
            mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), true));
        } else {
            mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), false));
        }

        return mvn;
    }

    @RequestMapping("/myinfo_manage")
    public ModelAndView myInfoManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/myinfo_manage");
        return mvn;
    }

    @RequestMapping("/otp_manage")
    public ModelAndView otpManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/otp_manage");

        User existUser = userRepository.findOne(user.getId());
        if((existUser.getOtpHash() == null) || existUser.getOtpHash().equals("")) {
            mvn.addObject("otpFlag", false);
        } else if(existUser.getOtpStatus().equals(OtpStatus.N.name()) || existUser.getOtpStatus().equals(OtpStatus.P.name())) {
            mvn.addObject("otpFlag", false);
        } else {
            mvn.addObject("otpFlag", true);
        }

        return mvn;
    }

    @RequestMapping("/auth_manage")
    public ModelAndView authManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/auth_manage");
        mvn.addObject("levels", levelService.getAllGroups());
        mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), true));
        return mvn;
    }

    @RequestMapping("/access_manage")
    public ModelAndView accessManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/access_manage");
        return mvn;
    }

    @RequestMapping("/assets_manage")
    public ModelAndView assetsManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/assets_manage");
        mvn.addObject("walletInfos", walletService.getMyWallets(user.getId(), user.getLevel(), true));
        return mvn;
    }

    @RequestMapping("/support_manage")
    public ModelAndView supportManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/support_manage");
        return mvn;
    }

    @RequestMapping("/support_view")
    public ModelAndView supportView(@RequestParam String id) {
        Support support = supportService.getSupport(Long.parseLong(id));
        ModelAndView mvn = new ModelAndView("dashboard/support_view");
        mvn.addObject("support", supportService.getSupport(Long.parseLong(id)));
        mvn.addObject("context", support);
        return mvn;
    }

    @RequestMapping("/faq_manage")
    public ModelAndView faqManage(@ModelAttribute User user) {
        ModelAndView mvn = new ModelAndView("dashboard/faq_manage");
        return mvn;
    }

    public boolean validCoinEnum(String coinName) {
        boolean validCheck = false;
        for(CoinEnum coinEnum : CoinEnum.values()) {
            if(coinEnum.name().equals(coinName)) {
                validCheck = true;
                break;
            }
        }
        return validCheck;
    }
}
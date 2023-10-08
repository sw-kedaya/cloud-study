package cn.itcast.account.service.impl;

import cn.itcast.account.entity.Account;
import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountTCCService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTCCServiceImpl implements AccountTCCService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountFreezeMapper accountFreezeMapper;

    @Override
    @Transactional
    public void deduct(String userId, int money) {
        String xid = RootContext.getXID();

        // 0.处理悬挂问题
        AccountFreeze oldFreeze = accountFreezeMapper.selectById(xid);
        if (oldFreeze != null){
            //如果它不为空，说明已经进入过cancel，直接结束
            return;
        }

        // 1.扣减可用余额
        accountMapper.deduct(userId, money);
        // 2.记录冻结金额和事务状态(try)
        AccountFreeze freeze = new AccountFreeze();
        freeze.setXid(xid);
        freeze.setUserId(userId);
        freeze.setFreezeMoney(money);
        freeze.setState(AccountFreeze.State.TRY);
        accountFreezeMapper.insert(freeze);
    }

    @Override
    public boolean confirm(BusinessActionContext ctx) {
        // 1.获取事务id
        String xid = ctx.getXid();
        // 2.根据id删除冻结记录
        int count = accountFreezeMapper.deleteById(xid);

        return count == 1;
    }

    @Override
    public boolean cancel(BusinessActionContext ctx) {
        // 1.查询冻结记录
        String xid = ctx.getXid();
        String userId = ctx.getActionContext("userId").toString();

        AccountFreeze freeze = accountFreezeMapper.selectById(xid);

        // 1.1空回滚处理
        if (freeze == null) {
            freeze = new AccountFreeze();
            freeze.setXid(xid);
            freeze.setUserId(userId);
            freeze.setFreezeMoney(0);
            freeze.setState(AccountFreeze.State.CANCEL);
            accountFreezeMapper.insert(freeze);

            return true;
        }

        // 1.2幂等处理
        if (freeze.getState() == AccountFreeze.State.CANCEL){
            // 1.2.1如果状态已经是cancel说明回滚过
            return true;
        }

        // 2.恢复可用余额
        accountMapper.refund(freeze.getUserId(), freeze.getFreezeMoney());
        // 3.将冻结金额记录修改为0并且将状态改为CANCEL
        freeze.setState(AccountFreeze.State.CANCEL);
        freeze.setFreezeMoney(0);

        int count = accountFreezeMapper.updateById(freeze);

        return count == 1;
    }
}

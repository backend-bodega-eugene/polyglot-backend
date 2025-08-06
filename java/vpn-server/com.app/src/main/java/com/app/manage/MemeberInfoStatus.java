package com.app.manage;

import com.app.controller.dto.responsedto.MemeberInfoStatusVO;

import java.util.ArrayList;
import java.util.List;

public class MemeberInfoStatus {
    private MemeberInfoStatus() {

    }

    private static MemeberInfoStatus myMemberInfoStatus;

    public static synchronized MemeberInfoStatus getInstance() {
        if (myMemberInfoStatus == null) {
            myMemberInfoStatus = new MemeberInfoStatus();
        }
        return myMemberInfoStatus;
    }

    public List<MemeberInfoStatusVO> lst = new ArrayList<>();

    public void AddMemeberInfoStatusVO(MemeberInfoStatusVO vo) {
        MemeberInfoStatusVO tempvo = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getMemId() == vo.getMemId()) {
                lst.get(i).setMemStatus(vo.getMemStatus());
                tempvo = lst.get(i);
                break;
            }
        }
        if (tempvo == null) {
            lst.add(vo);
        }

    }

    public MemeberInfoStatusVO GetVO(long memid) {
        MemeberInfoStatusVO temp = null;
        for (MemeberInfoStatusVO vo : lst
        ) {
            if (vo.getMemId() == memid) {

                temp = vo;
                break;
            }
        }
        if (temp == null) {
            temp = new MemeberInfoStatusVO();

        }
        return temp;

    }

    public List<MemeberInfoStatusVO> GetList() {
        return lst;
    }
}

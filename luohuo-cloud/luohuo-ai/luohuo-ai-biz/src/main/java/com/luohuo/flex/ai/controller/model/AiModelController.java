package com.luohuo.flex.ai.controller.model;

import cn.hutool.core.util.ObjUtil;
import com.luohuo.flex.ai.common.pojo.PageResult;
import com.luohuo.flex.ai.controller.model.vo.model.AiModelPageReqVO;
import com.luohuo.flex.ai.controller.model.vo.model.AiModelRespVO;
import com.luohuo.flex.ai.controller.model.vo.model.AiModelSaveMyReqVO;
import com.luohuo.flex.ai.controller.model.vo.model.AiModelSaveReqVO;
import com.luohuo.flex.ai.dal.model.AiModelDO;
import com.luohuo.flex.ai.enums.CommonStatusEnum;
import com.luohuo.flex.ai.service.model.AiModelService;
import com.luohuo.flex.ai.utils.BeanUtils;
import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.luohuo.basic.base.R.success;
import static com.luohuo.basic.utils.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI 模型")
@RestController
@RequestMapping("/model")
@Validated
public class AiModelController {

	@Resource
	private AiModelService modelService;

	@PostMapping("/create")
	@Operation(summary = "创建模型")
	public R<Long> createModel(@Valid @RequestBody AiModelSaveReqVO createReqVO) {
		if (createReqVO.getPublicStatus() == null || !createReqVO.getPublicStatus()) {
			AiModelSaveMyReqVO myReqVO = BeanUtils.toBean(createReqVO, AiModelSaveMyReqVO.class);
			return success(modelService.createModelMy(myReqVO, ContextUtil.getUid()));
		}
		return success(modelService.createModel(createReqVO));
	}

	@PutMapping("/update")
	@Operation(summary = "更新模型")
	public R<Boolean> updateModel(@Valid @RequestBody AiModelSaveReqVO updateReqVO) {
		AiModelDO model = modelService.getModel(updateReqVO.getId());
		if (model == null) {
			return success(false);
		}

		Long uid = ContextUtil.getUid();
		if (Boolean.FALSE.equals(model.getPublicStatus())) {
			if (ObjUtil.notEqual(model.getUserId(), uid)) {
				return success(false); // 无权限
			}
			AiModelSaveMyReqVO myReqVO = BeanUtils.toBean(updateReqVO, AiModelSaveMyReqVO.class);
			modelService.updateModelMy(myReqVO, ContextUtil.getUid());
		} else {
			if (uid < 10937855681025L){
				modelService.updateModel(updateReqVO);
			}
		}
		return success(true);
	}

	@DeleteMapping("/delete")
	@Operation(summary = "删除模型")
	@Parameter(name = "id", description = "编号", required = true)
	public R<Boolean> deleteModel(@RequestParam("id") Long id) {
		AiModelDO model = modelService.getModel(id);
		if (model == null) {
			return success(false);
		}

		Long uid = ContextUtil.getUid();
		if (Boolean.FALSE.equals(model.getPublicStatus())) {
			if (ObjUtil.notEqual(model.getUserId(), uid)) {
				return success(false); // 无权限
			}
			modelService.deleteModelMy(id, ContextUtil.getUid());
		} else {
			if (uid < 10937855681025L){
				modelService.deleteModel(id);
			}
		}
		return success(true);
	}

	@GetMapping("/get")
	@Operation(summary = "获得模型")
	@Parameter(name = "id", description = "编号", required = true, example = "1024")
	public R<AiModelRespVO> getModel(@RequestParam("id") Long id) {
		AiModelDO model = modelService.getModel(id);
		return success(BeanUtils.toBean(model, AiModelRespVO.class));
	}

	@GetMapping("/page")
	@Operation(summary = "获得模型分页（包含系统公开模型和用户私有模型）")
	public R<PageResult<AiModelRespVO>> getModelPage(@Valid AiModelPageReqVO pageReqVO) {
		PageResult<AiModelDO> pageResult = modelService.getModelMyPage(pageReqVO, ContextUtil.getUid());
		return success(BeanUtils.toBean(pageResult, AiModelRespVO.class));
	}

	@GetMapping("/simple-list")
	@Operation(summary = "获得模型列表（包含系统公开模型和用户私有模型）")
	@Parameter(name = "type", description = "类型", required = true, example = "1")
	@Parameter(name = "platform", description = "平台", example = "midjourney")
	public R<List<AiModelRespVO>> getModelSimpleList(@RequestParam("type") Integer type, @RequestParam(value = "platform", required = false) String platform) {
		List<AiModelDO> list = modelService.getModelListByStatusAndTypeAndUserId(CommonStatusEnum.ENABLE.getStatus(), type, platform, ContextUtil.getUid());
		return success(convertList(list, model -> new AiModelRespVO().setId(model.getId())
				.setName(model.getName()).setModel(model.getModel()).setPlatform(model.getPlatform())));
	}

}
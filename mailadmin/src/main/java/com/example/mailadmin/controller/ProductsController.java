package com.example.mailadmin.controller;


import com.example.mailadmin.dto.AddProductsDTO;
import com.example.mailadmin.dto.EditProductsDTO;
import com.example.mailadmin.dto.ProductsPageQueryDTO;
import com.example.mailadmin.dto.StockUpdateDTO;
import com.example.mailadmin.entity.Products;
import com.example.mailadmin.service.ProductsService;
import com.example.result.PageResult;
import com.example.result.Result;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.result.Result.success;


/**
 * 商品Controller
 *
 * @author sunxiaoming
 * @date 2026-01-31
 */
@Slf4j
@RestController
@RequestMapping("/products/products")
@Api(tags = "商品管理")
public class ProductsController
{
    @Autowired
    private ProductsService productsService;
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket.name}")
    private String bucketName;
    
    @Value("${minio.url}")
    private String endpoint;

    /**
     * 分页查询商品列表
     */

    @GetMapping("/page")
    @ApiOperation(value = "分页查询商品列表", notes = "根据条件分页查询商品列表，支持商品名称、分类等条件搜索")
    public Result<PageResult> Page(@ApiParam(name = "productsPageQueryDTO", value = "商品分页查询参数", required = true) @ModelAttribute ProductsPageQueryDTO productsPageQueryDTO)
    {

        PageResult pageResult = productsService.PageQuery(productsPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 获取商品详细信息
     */
    @GetMapping(value = "/{id}")
    @ApiOperation(value = "获取商品详细信息", notes = "根据商品ID获取商品的详细信息")
    public Result<Products> getInfo(@ApiParam(name = "id", value = "商品ID", required = true) @PathVariable("id") Long id)
    {
        return Result.success(productsService.selectProductsById(id));
    }

    /**
     * 新增商品
     */

    @PostMapping("/add")
    @ApiOperation(value = "新增商品", notes = "添加新的商品信息")
    public Result add(@ApiParam(name = "addProductsDTO", value = "新增商品信息", required = true) @RequestBody AddProductsDTO addProductsDTO)
    {
        productsService.insertProducts(addProductsDTO);
        return Result.success();
    }

    /**
     * 修改商品
     */

    @PutMapping("/edit")
    @ApiOperation(value = "修改商品", notes = "更新商品的详细信息")
    public Result edit(@ApiParam(name = "editProductsDTO", value = "修改商品信息", required = true) @RequestBody EditProductsDTO editProductsDTO)
    {
        productsService.updateProducts(editProductsDTO);
        return Result.success();
    }

    /**
     * 删除商品
     */

    @DeleteMapping("/remove/{ids}")
    @ApiOperation(value = "删除商品", notes = "批量删除商品")
    public Result remove(@ApiParam(name = "ids", value = "商品ID数组", required = true) @PathVariable Long[] ids)
    {
      productsService.deleteProductsByIds(ids);
      return Result.success();
    }

    /**
     * 更新商品库存
     */
    @PutMapping("/updateStock")
    @ApiOperation(value = "更新商品库存", notes = "更新单个商品的库存数量")
    public Result updateStock(@ApiParam(name = "id", value = "商品ID", required = true) @RequestParam Long id, 
                             @ApiParam(name = "stock", value = "库存数量", required = true) @RequestParam Integer stock)
    {
        productsService.updateStock(id, stock);
        return Result.success();
    }

    /**
     * 批量更新商品库存
     */
    @PutMapping("/batchUpdateStock")
    @ApiOperation(value = "批量更新商品库存", notes = "批量更新多个商品的库存数量")
    public Result batchUpdateStock(@ApiParam(name = "stockUpdateDTOs", value = "库存更新列表", required = true) @RequestBody List<StockUpdateDTO> stockUpdateDTOs)
    {
        productsService.batchUpdateStock(stockUpdateDTOs);
        return Result.success();
    }

    /**
     * 上架商品
     */
    @PutMapping("/enable/{ids}")
    @ApiOperation(value = "上架商品", notes = "批量将商品状态设置为上架")
    public Result enable(@ApiParam(name = "ids", value = "商品ID数组", required = true) @PathVariable Long[] ids)
    {
        productsService.enable(ids);
        return Result.success();
    }

    /**
     * 下架商品
     */
    @PutMapping("/disable/{ids}")
    @ApiOperation(value = "下架商品", notes = "批量将商品状态设置为下架")
    public Result disable(@ApiParam(name = "ids", value = "商品ID数组", required = true) @PathVariable Long[] ids)
    {
        productsService.disable(ids);
        return Result.success();
    }
    
   /* *//**
     * 上传商品图片
     *//*
    @PostMapping("/upload")
    @ApiOperation(value = "上传商品图片", notes = "上传商品图片到Minio并返回图片URL")
    public Result<String> upload(@ApiParam(name = "file", value = "商品图片文件", required = true) @RequestParam("file") MultipartFile file)
    {
        try {
            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            
            // 上传文件到Minio
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 生成访问URL
            String imageUrl = endpoint + "/" + bucketName + "/" + fileName;
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }*/
@PostMapping("/upload")
@ApiOperation(value = "上传商品图片", notes = "上传商品图片到Minio并返回图片URL")
public Result<String> upload(@RequestParam("file") MultipartFile file) {
    // 文件校验
    if (file.isEmpty()) {
        return Result.error("文件不能为空");
    }

    // 文件类型校验
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        return Result.error("只支持图片文件上传");
    }

    // 文件大小限制（例如5MB）
    if (file.getSize() > 5 * 1024 * 1024) {
        return Result.error("文件大小不能超过5MB");
    }

    try {
        String fileName = UUID.randomUUID().toString() +
                getFileExtension(file.getOriginalFilename());

        // 上传到Minio
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                        .build()
        );

        // 生成完整的访问URL
        String imageUrl = String.format("%s/%s/%s",
                endpoint.replaceFirst("//$", ""), bucketName, fileName);

        return Result.success(imageUrl);
    } catch (Exception e) {
        log.error("图片上传失败", e);
        return Result.error("上传失败: " + e.getMessage());
    }
}

    // 辅助方法：获取文件扩展名
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

}




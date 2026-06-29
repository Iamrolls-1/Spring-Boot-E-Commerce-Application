package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileService fileService;
    @Value("${project.image.path}")
    private String uploadPath;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean ifProductNotPresent = true;
        List<Product> products = category.getProduct();

        for (Product p : products) {
            if (p.getProductName().equalsIgnoreCase(productDto.getProductName())) {
                ifProductNotPresent = false;
                break;
            }
        }

        if (ifProductNotPresent) {
            Product product = modelMapper.map(productDto, Product.class);

            product.setImage("default.png");
            product.setCategory(category);

            double discountamount = product.getPrice() * (product.getDiscount() * 0.01);
            double specialPrice = product.getPrice() - discountamount;

            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            ProductDTO productDTO = modelMapper.map(savedProduct, ProductDTO.class);

            return productDTO;
        } else {
            throw new APIException("Product already exist!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = "asc".equalsIgnoreCase(sortOrder) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        System.out.println(pageProducts.getContent());
        List<Product> productList = pageProducts.getContent();
        System.out.println(productList);

        List<ProductDTO> productDTOS = productList
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (productList.isEmpty()) {
            throw new APIException("No Products Exist");
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;

    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Product> productList = productRepository.findByCategoryOrderByPriceAsc(category);

        List<ProductDTO> productDTOS = productList
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);
        List<ProductDTO> productDTO = products
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse(productDTO);

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDto) {
        Product productFromDb = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productID", productId));

        Product product = modelMapper.map(productDto, Product.class);

        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

        ProductDTO productDTO = modelMapper.map(savedProduct, ProductDTO.class);

        return productDTO;
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(product);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        return productDTO;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Upload Image to the server
        String path = uploadPath;
        String fileName = fileService.uploadImage(path, image);

        //Updating the new file name to the product
        productFromDb.setImage(fileName);

        //Save the product
        Product updatedProduct = productRepository.save(productFromDb);

        //return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


}

import React, { Component } from 'react'
import styled from 'styled-components';

const Wrapper = styled.div`
  display: flex;
  text-align: left;
  padding: 5px 0;
  align-items: center;
`

const ImageDiv = styled.div`
  display: flex;
  align-items: center;
  min-width: ${props => props.imgSize ? props.imgSize * 2 + 'px' : '24px'};
  height: ${props => props.imgSize ? props.imgSize * 2 + 'px' : '24px'};
  line-height: 2;
  text-align: center;
  justify-content: center;
  border-radius: 50%;
  background-color: ${props => props.imgColor ? props.imgColor : '#ffffff00'};
  border: 2px solid #00000022;
`

const Image = styled.div`
  font-size: ${props => props.imgSize ? props.imgSize + 'px' : '16px'};
  width: ${props => props.imgSize ? props.imgSize + 'px' : '16px'};
  color: ${props => props.color};
`

const TextContent = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-left: 15px;
  width: 100%;
`

const Label = styled.div`
  color: white;
  font-weight: 300;
  opacity: 0.7;
`

const Value = styled.div`
  font-size: 20px;
  letter-spacing: 1.5px;
`

export class ImageContent extends Component {
  render() {
    const { image } = this.props;

    return (
      <Wrapper>
        <ImageDiv {...this.props}>
          <Image {...this.props}>{image}</Image>
        </ImageDiv>
        <TextContent>
          {this.props.children}
        </TextContent>
      </Wrapper>
    )
  }
}

export default class ImageTitleValue extends Component {
  render() {
    const { title, value, image, color } = this.props;

    return (
      <Wrapper>
        <Image {...this.props}>{image}</Image>
        <TextContent>
          { title && <Label>{title}</Label> }
          { value && <Value>{value}</Value> } 
        </TextContent>
      </Wrapper>
    )
  }
}

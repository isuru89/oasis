import React, { Component } from 'react'
import styled from 'styled-components';
import PopOver from "antd/lib/popover";

const Wrapper = styled.div`
  display: flex;
  padding: 10px;
  margin-right: 10px;
  border-radius: 10px;
  border: 1px solid #ffffff00;

  &:hover {
    border: 1px solid #ccc;
  }
`

const BadgeCount = styled.span`
  user-select: none;
  position: absolute;
  background-color: hsl(203, 43%, 16%);
  color: hsl(203, 80%, 56%);
  border: 1px solid hsl(203, 43%, 36%);
  padding: 2px 4px;
  border-radius: 50%;
  top: 0px;
  right: 5px;
`

const Image = styled.div`
  position: relative;
  padding-right: 10px;
  opacity: ${props => props.acquired ? 1 : 0.1};
`

const Details = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  width: 180px;
`

const Title = styled.div`
  line-height: 2;
  font-weight: 700;
`

const Description = styled.div`
  font-size: 12px;
  flex: 1;
`

const MetaInfo = styled.div`
  justify-self: flex-end;
  line-height: 2;
  font-size: 12px;
  font-style: italic;
`

class BadgeDetails extends Component {

  render() {
    return (
      <div>
        <div>{this.props.description}</div>
        <div style={{height: '40px'}}></div>
        {this.props.achievedDate && <MetaInfo>You last achieved this on 9th Feb.</MetaInfo>}
      </div>
    )
  }
}

export default class Badge extends Component {
  render() {
    const { image, title, description, achievedDate, count = 5, imageOnly = true, acquired = true } = this.props;

    return (
      <Wrapper>
        <Image acquired={acquired} {...this.props}>
          <img src={image} width={80} height={90} />
          { acquired && count > 1 && !imageOnly &&
            <PopOver content={<BadgeDetails {...this.props}/>} title={<Title>{title}</Title>}>
              <BadgeCount>x{count}</BadgeCount> 
            </PopOver>
          }
        </Image>
        {
          !imageOnly && <Details>
            <Title>{title}</Title>
            <Description>{description}</Description>
          </Details>
        }
      </Wrapper>
    )
  }
}
